package com.example.librarian.entity.libbook;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_event_log", schema = "libbook")
public class InventoryEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_log_id")
    private Long id;

    @Column(name = "inventory_Id", nullable = false, length = 100)
    private String inventoryId;


    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "reference_id", nullable = false, length = 100)
    private String referenceId;



    @Column(name = "status", nullable = false, length = 16)
    private String status;


    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public InventoryEventLog() {
        this.referenceId = UUID.randomUUID().toString();
    }

    public Long getId() {
        return id;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }


    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

