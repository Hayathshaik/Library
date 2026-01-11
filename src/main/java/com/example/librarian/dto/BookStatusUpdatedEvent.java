package com.example.librarian.dto;

public class BookStatusUpdatedEvent {

    private String eventName = "bookstatusupdated";
    private String bookId;
    private String userId;
    private String newStatus;
    private String correlationId;
    private Long loanId;

    public static BookStatusUpdatedEvent of(String bookId, String userId, String newStatus, Long loanId, String correlationId) {
        BookStatusUpdatedEvent evt = new BookStatusUpdatedEvent();
        evt.setBookId(bookId);
        evt.setUserId(userId);
        evt.setNewStatus(newStatus);
        evt.setLoanId(loanId);
        evt.setCorrelationId(correlationId);
        return evt;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }
}
