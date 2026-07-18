package com.cnkart.inventory.event;

import com.cnkart.inventory.model.OutboxEvent;
import com.cnkart.inventory.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPoller {

    private static final String TOPIC = "cnkart.inventory.events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${outbox.poll.interval:5000}")
    public void pollOutbox() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events. Publishing to Kafka...", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                org.springframework.util.concurrent.ListenableFuture<org.springframework.kafka.support.SendResult<String, String>> future = 
                    kafkaTemplate.send(TOPIC, event.getAggregateId(), event.getPayload());
                
                future.addCallback(new org.springframework.util.concurrent.ListenableFutureCallback<org.springframework.kafka.support.SendResult<String, String>>() {
                    @Override
                    public void onSuccess(org.springframework.kafka.support.SendResult<String, String> result) {
                        event.setStatus("PUBLISHED");
                        outboxEventRepository.save(event);
                        log.debug("Published outbox event {} for inventory {}", event.getId(), event.getAggregateId());
                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("Failed to publish outbox event {}: {}", event.getId(), ex.getMessage());
                        event.setStatus("FAILED");
                        outboxEventRepository.save(event);
                    }
                });
            } catch (Exception e) {
                log.error("Serialization or immediate error for outbox event {}: {}", event.getId(), e.getMessage());
                event.setStatus("FAILED");
                outboxEventRepository.save(event);
            }
        }
    }
}
