package com.example.librarian.service;

import com.example.librarian.dto.BookInventoryRequest;
import com.example.librarian.entity.libbook.Book;
import com.example.librarian.entity.libbook.BookInventory;
import com.example.librarian.repository.libbook.BookInventoryRepository;
import com.example.librarian.repository.libbook.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BooksInventoryDomainService {

    private final BookRepository bookRepository;
    private final BookInventoryRepository bookInventoryRepository;

    public BooksInventoryDomainService(BookRepository bookRepository, BookInventoryRepository bookInventoryRepository) {
        this.bookRepository = bookRepository;
        this.bookInventoryRepository = bookInventoryRepository;
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
            newBook.setStatus("AVAILABLE");
            return bookRepository.save(newBook);
        });

        BookInventory bookInventory = new BookInventory();
        bookInventory.setBookId(String.valueOf(request.getBookId()));
        bookInventory.setQuantity(request.getQuantity());
        bookInventory.setStatus("AVAILABLE");
        return bookInventoryRepository.save(bookInventory);
    }
}
