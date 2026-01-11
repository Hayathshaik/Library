package com.example.librarian.controller;

import com.example.librarian.dto.CheckoutRequest;
import com.example.librarian.dto.ReturnRequest;
import com.example.librarian.dto.CheckoutResponse;
import com.example.librarian.dto.BookInventoryRequest;
import com.example.librarian.entity.libbook.BookInventory;
import com.example.librarian.service.LibrarianService;
import com.example.librarian.service.BooksInventoryDomainService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping
public class LibrarianController {

    private final LibrarianService librarianService;
    private final BooksInventoryDomainService booksInventoryDomainService;

    public LibrarianController(LibrarianService librarianService, BooksInventoryDomainService booksInventoryDomainService) {
        this.librarianService = librarianService;
        this.booksInventoryDomainService = booksInventoryDomainService;
    }

    @PostMapping("/books/checkout")
    public ResponseEntity<CheckoutResponse> checkoutBook(@Valid @RequestBody CheckoutRequest request) {
        Long loanId = librarianService.checkoutBook(request.getUserId(), request.getBookId());
        return ResponseEntity.ok(new CheckoutResponse(loanId));
    }

    @PostMapping("/books/return")
    public ResponseEntity<CheckoutResponse> returnBook(@Valid @RequestBody ReturnRequest request) {
        Long loanId = librarianService.returnBook(request.getUserId(), request.getBookId());
        return ResponseEntity.ok(new CheckoutResponse(loanId));
    }

    @PostMapping("/books/inventory")
    public ResponseEntity<BookInventory> bookInventory(@Valid @RequestBody BookInventoryRequest request) {
        BookInventory bookInventory = booksInventoryDomainService.addBookInventory(request);
        return ResponseEntity.ok(bookInventory);
    }
}
