package com.example.librarian.outbox;

import com.example.librarian.entity.libbook.OutboxEvent;
import com.example.librarian.repository.libbook.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxDispatcher {
    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxDispatcher(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void scheduledDispatch() {
        try {
            // This is the background task that can fail during test startup
            dispatch();
        } catch (Exception e) {
            log.debug("Background dispatch suppressed or failed: {}", e.getMessage());
        }
    }

    @Transactional("libbookTransactionManager")
    public int dispatch() {
        List<OutboxEvent> events = outboxRepository.findTop100ByProcessedFalseOrderByCreatedAtAsc();
        int count = 0;

        for (OutboxEvent event : events) {
            try {
                if (event.isProcessed()) {
                    continue;
                }
                // Determine topic and send to Kafka
                String topic = "BookCheckedOut".equalsIgnoreCase(event.getEventType()) ? "book_events" : event.getEventType();

                kafkaTemplate.send(topic, event.getNew_Status(), event.getPayloadJson())
                        .get(5, TimeUnit.SECONDS);

                event.setProcessed(true);
                outboxRepository.save(event);
                count++;
            } catch (Exception e) {
                log.error("Failed to dispatch event {}: {}", event.getEventId(), e.getMessage());
            }
        }
        return count;

    }
}