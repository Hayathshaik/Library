package com.example.librarian.dto;

public class CheckoutResponse {
    private Long loanId;

    public CheckoutResponse(Long loanId) { this.loanId = loanId; }
    public Long getLoanId() { return loanId; }
    public void setLoanId(Long loanId) { this.loanId = loanId; }
}

