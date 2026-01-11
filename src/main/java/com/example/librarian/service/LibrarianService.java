package com.example.librarian.service;

import com.example.librarian.dto.BookStatusUpdatedEvent;
import com.example.librarian.entity.libbook.Book;
import com.example.librarian.entity.libbook.Loan;
import com.example.librarian.repository.libbook.BookRepository;
import com.example.librarian.repository.libbook.LoanRepository;
import com.example.librarian.repository.libbook.OutboxRepository;
import com.example.librarian.repository.libdemo.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.librarian.entity.libbook.OutboxEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class LibrarianService {

    private static final Logger log = LoggerFactory.getLogger(LibrarianService.class);
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public LibrarianService(UserRepository userRepository, BookRepository bookRepository, LoanRepository loanRepository, OutboxRepository outboxRepository, ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional(transactionManager = "libbookTransactionManager")
    public Long checkoutBook(String userId, String bookId) {
        log.info("Processing checkout request for userId: {}, bookId: {}", userId, bookId);

        // FIX: Remove numeric parsing for bookId. Keep it for userId.
        Long userIdNumeric = Long.valueOf(userId);

        // 1. Check User in libdemo
        if (!userRepository.existsById(userIdNumeric)) {
            log.error("Checkout failed: User {} not found in libdemo", userIdNumeric);
            throw new ResponseStatusException(BAD_REQUEST, "User not found");
        }

        // 2. Check Book using String ID
        Optional<Book> bookOpt = bookRepository.findById(bookId); // No longer bookIdNumeric
        if (bookOpt.isEmpty()) {
            log.error("Checkout failed: Book {} not found", bookId);
            throw new ResponseStatusException(BAD_REQUEST, "Book not found");
        }

        Book book = bookOpt.get();
        if (!"Available".equalsIgnoreCase(book.getStatus())) {
            log.error("Checkout failed: Book {} is {}", bookId, book.getStatus());
            throw new ResponseStatusException(BAD_REQUEST, "Book is not available");
        }

        // 3. Check for Existing Loans
        if (loanRepository.existsByBookIdAndState(bookId, "Open")) {
            log.error("Checkout failed: Book {} already has an active loan", bookId);
            throw new ResponseStatusException(BAD_REQUEST, "Book already has an open loan");
        }

        // --- Logic Execution ---
        Loan loan = new Loan();
        loan.setBookId(bookId); // Loan.setBookId must accept a String
        loan.setUserId(userIdNumeric);
        loan.setState("Open");
        loan.setCheckoutDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusWeeks(2));

        String correlationId = UUID.randomUUID().toString();
        loan.setCorrelationId(correlationId);
        Loan saved = loanRepository.save(loan);

        book.setStatus("CheckedOut");
        bookRepository.save(book);

        // Dispatch Events
        BookStatusUpdatedEvent statusEvent = BookStatusUpdatedEvent.of(bookId, userId, "Checkout", saved.getId(), correlationId);
        sendBookStatusUpdated(statusEvent);

        OutboxEvent evt = new OutboxEvent();
        evt.setEventType("BookCheckedOut");
        evt.setCreatedAt(LocalDateTime.now());
        evt.setNew_Status("CheckedOut");
        evt.setBookId(bookId); // OutboxEvent.setBookId must accept a String
        evt.setUserId(userIdNumeric);

        Map<String, Object> payload = new HashMap<>();
        payload.put("loanId", saved.getId());
        payload.put("userId", userIdNumeric);
        payload.put("bookId", bookId);
        payload.put("correlationId", correlationId);

        try {
            evt.setPayloadJson(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload", e);
            throw new RuntimeException(e);
        }

        outboxRepository.save(evt);
        return saved.getId();
    }

    @Transactional(transactionManager = "libbookTransactionManager")
    public Long returnBook(String userId, String bookId) {
        Long userIdNumeric = Long.valueOf(userId);

        Optional<Loan> loanOpt = loanRepository.findOpenLoanByUserIdAndBookId(userIdNumeric, bookId);
        if (loanOpt.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No open loan found");
        }
        Loan loan = loanOpt.get();

        loan.setState("Closed");
        loanRepository.save(loan);

        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Book not found");
        }
        Book book = bookOpt.get();
        book.setStatus("Available");
        bookRepository.save(book);
        loan.setReturndate(LocalDateTime.now());
        loanRepository.save(loan);
        OutboxEvent evt = new OutboxEvent();
        evt.setEventType("BookReturned");
        evt.setCreatedAt(LocalDateTime.now());
        evt.setNew_Status("Returned");
        evt.setBookId(bookId);
        evt.setUserId(userIdNumeric);

        Map<String, Object> payload = new HashMap<>();
        payload.put("loanId", loan.getId());
        payload.put("userId", userIdNumeric);
        payload.put("bookId", bookId);

        try {
            evt.setPayloadJson(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        outboxRepository.save(evt);
        return loan.getId();
    }

    private void sendBookStatusUpdated(BookStatusUpdatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("book_events", event.getCorrelationId(), payload);
        } catch (JsonProcessingException e) {
            log.error("Kafka failure", e);
            throw new RuntimeException(e);
        }
    }
    // parseBookId method is deleted because we use String now
}