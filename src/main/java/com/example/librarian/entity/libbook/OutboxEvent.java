package com.example.librarian.entity.libbook;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_event_outbox", schema = "libbook")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "book_id")
    private String bookId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "new_status")
    private String newStatus;

    @Column(length = 50)
    private String status = "PENDING";

    @Column(name = "payload_json", columnDefinition = "json")
    private String payloadJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // @Column(name = "publish_at")
    // private LocalDateTime publishAt;

    @Column(name = "processed")
    private boolean processed = false;

    // --- GETTERS AND SETTERS ---

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    // CRITICAL: This is the method the Dispatcher was missing
    public String getNew_Status() { return newStatus; }
    public void setNew_Status(String new_Status) {
        this.newStatus = new_Status;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // CRITICAL: This is the method the Dispatcher was missing
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    //    public LocalDateTime getPublishAt() { return publishAt; }
    //    public void setPublishAt(LocalDateTime publishAt) { this.publishAt = publishAt; }
    //
     public boolean isProcessed() { return processed; }
     public void setProcessed(boolean processed) { this.processed = processed; }

}