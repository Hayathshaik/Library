// File: src/main/java/com/example/librarian/consumer/InventorySyncConsumer.java
package com.example.librarian.consumer;

import com.example.librarian.entity.libbook.BookInventory;
import com.example.librarian.entity.libbook.InventoryEventLog;
import com.example.librarian.repository.libbook.BookInventoryRepository;
import com.example.librarian.repository.libbook.InventoryEventLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InventorySyncConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventorySyncConsumer.class);

    private final BookInventoryRepository inventoryRepository;
    private final InventoryEventLogRepository logRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InventorySyncConsumer(BookInventoryRepository inventoryRepository,
                                 InventoryEventLogRepository logRepository) {
        this.inventoryRepository = inventoryRepository;
        this.logRepository = logRepository;
    }

    @KafkaListener(topics = "inventory_updates", containerFactory = "inventoryKafkaListenerContainerFactory")
    @Transactional("libbookTransactionManager")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String bookId = record.key();
        String payload = record.value();

        log.debug("Inventory message received topic={} partition={} offset={} key={} payload={}",
                record.topic(), record.partition(), record.offset(), bookId, payload);

        try {
            JsonNode root = objectMapper.readTree(payload);
            String payloadBookId = root.path("bookId").asText(null);
            int newQty = root.path("quantity").asInt(-1);
            if (payloadBookId == null || !payloadBookId.equals(bookId) || newQty < 0) {
                throw new IllegalArgumentException("Invalid payload for bookId=" + bookId);
            }

            BookInventory inventory = inventoryRepository.findByBookId(bookId)
                    .orElseThrow(() -> new IllegalStateException("Inventory not found for book " + bookId));

            inventory.setQuantity(newQty);
            inventoryRepository.save(inventory);

            InventoryEventLog logEntry = new InventoryEventLog();
            logEntry.setInventoryId(String.valueOf(inventory.getId()));
            logEntry.setEventType("INVENTORY_SYNCED");
            logEntry.setPayload(payload);
            logEntry.setStatus(inventory.getStatus());
            logRepository.save(logEntry);

            ack.acknowledge();
            log.debug("Inventory sync committed key={} newQty={} logId={}", bookId, newQty, logEntry.getId());
        } catch (JsonProcessingException ex) {
            log.error("Failed to parse inventory message key={} offset={}: {}", bookId, record.offset(), ex.getMessage(), ex);
            throw new IllegalStateException("Invalid inventory message payload", ex);
        } catch (Exception ex) {
            log.error("Failed to process inventory message key={} offset={}: {}", bookId, record.offset(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
