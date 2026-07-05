package com.cnkart.order.event;

import com.cnkart.order.model.OutboxEvent;
import com.cnkart.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPoller {

    private static final String TOPIC = "cnkart.order.events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${outbox.poll.interval:5000}")
    @Transactional
    public void pollOutbox() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events. Publishing to Kafka...", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send(TOPIC, event.getAggregateId(), event.getPayload());
                event.setStatus("PUBLISHED");
                outboxEventRepository.save(event);
                log.debug("Published outbox event {} for order {}", event.getId(), event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
                // Break to preserve ordering, or continue. We break to retry next time.
                break; 
            }
        }
    }
}
