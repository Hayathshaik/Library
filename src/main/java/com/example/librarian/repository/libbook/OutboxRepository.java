package com.example.librarian.repository.libbook;

import com.example.librarian.entity.libbook.OutboxEvent; // Updated class name
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Logic breakdown:
     * 1. Top100: Limits batch size
     * 2. ProcessedFalse: Maps to 'processed' field in OutboxEvent
     * 3. OrderByCreatedAtAsc: Maps to 'createdAt' field in OutboxEvent
     */
    List<OutboxEvent> findTop100ByProcessedFalseOrderByCreatedAtAsc();

}