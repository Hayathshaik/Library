package com.example.librarian.dto;

import jakarta.validation.constraints.NotBlank;

public class ReturnRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String bookId;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
}

