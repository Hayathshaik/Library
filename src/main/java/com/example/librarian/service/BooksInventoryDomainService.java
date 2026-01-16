package com.example.librarian.service;

import com.example.librarian.dto.BookInventoryRequest;
import com.example.librarian.dto.UpdateInventoryRequest;
import com.example.librarian.entity.libbook.Book;
import com.example.librarian.entity.libbook.BookInventory;
import com.example.librarian.entity.libbook.InventoryEventLog;
import com.example.librarian.repository.libbook.BookInventoryRepository;
import com.example.librarian.repository.libbook.BookRepository;
import com.example.librarian.repository.libbook.InventoryEventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class BooksInventoryDomainService {

    private static final Logger log = LoggerFactory.getLogger(BooksInventoryDomainService.class);

    private final BookRepository bookRepository;
    private final BookInventoryRepository bookInventoryRepository;
    private final InventoryEventLogRepository inventoryEventLogRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    public BooksInventoryDomainService(BookRepository bookRepository,
                                       BookInventoryRepository bookInventoryRepository,
                                       InventoryEventLogRepository inventoryEventLogRepository,
                                       KafkaTemplate<String, String> kafkaTemplate) {
        this.bookRepository = bookRepository;
        this.bookInventoryRepository = bookInventoryRepository;
        this.inventoryEventLogRepository = inventoryEventLogRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional("libbookTransactionManager")
    public BookInventory addBookInventory(BookInventoryRequest request) {
        // Look up book by numeric id (schema stores VARCHAR but Book uses numeric PK)
        String numericBookId = request.getBookId();
        bookRepository.findById(numericBookId).orElseGet(() -> {
            Book newBook = new Book();
            newBook.setBookid(request.getBookId());
            newBook.setTitle(request.getTitle());
            newBook.setAuthor(request.getAuthor());
            return bookRepository.save(newBook);
        });

        BookInventory bookInventory = new BookInventory();
        bookInventory.setBookId(String.valueOf(request.getBookId()));
        bookInventory.setQuantity(request.getQuantity());
        bookInventory.setStatus("Active");
        return bookInventoryRepository.save(bookInventory);
    }

    @Transactional("libbookTransactionManager")
    public BookInventory updateInventory(UpdateInventoryRequest request) {
        // Validate book exists
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        // Validate inventory exists and has stock
        BookInventory inventory = bookInventoryRepository.findByBookId(book.getBookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for book"));
        if (inventory.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Book inventory is empty");
        }

        // Update quantity
        inventory.setQuantity(request.getQuantity());
        inventory.setStatus("ACTIVE");
        BookInventory saved = bookInventoryRepository.save(inventory);

        // Build event payload
        String payload = "{\"bookId\":\"" + saved.getBookId() + "\",\"previous quantity\":" + inventory.getQuantity() + "\",\"quantity\":" + saved.getQuantity() + "}";

        // Synchronous Kafka send: ensure broker acks before we log
        try {
            kafkaTemplate.send("book_events", saved.getBookId(), payload).get();
        } catch (Exception ex) {
            log.error("Kafka send failed for bookId={}", saved.getBookId(), ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to publish inventory event to Kafka");
        }

        // Log to event table after Kafka ack
        InventoryEventLog logEntry = new InventoryEventLog();
        logEntry.setInventoryId(String.valueOf(saved.getId()));
        logEntry.setEventType("INVENTORY_UPDATED");
        logEntry.setPayload(payload);
        logEntry.setStatus(saved.getStatus());
        inventoryEventLogRepository.save(logEntry);

        return saved;
    }
}
