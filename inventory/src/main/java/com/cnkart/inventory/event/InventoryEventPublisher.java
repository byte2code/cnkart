package com.cnkart.inventory.event;

import com.cnkart.inventory.dto.InventoryReservationRequest;
import com.cnkart.inventory.dto.InventoryReservationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cnkart.inventory.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventPublisher {

    private static final String TOPIC = "cnkart.inventory.events";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void publishInventoryReserved(InventoryReservationRequest request, InventoryReservationResponse response) {
        publish(new InventoryReservedEvent(
                request.getOrderReference(),
                request.getSkuCode(),
                request.getQuantity(),
                response.getAvailableQuantity(),
                "InventoryReserved",
                response.getMessage()
        ), request.getOrderReference());
    }

    public void publishInventoryRejected(InventoryReservationRequest request, InventoryReservationResponse response) {
        publish(new InventoryRejectedEvent(
                request.getOrderReference(),
                request.getSkuCode(),
                request.getQuantity(),
                response.getAvailableQuantity(),
                "InventoryRejected",
                response.getMessage()
        ), request.getOrderReference());
    }

    private void publish(Object event, String key) {
        try {
            com.cnkart.inventory.model.OutboxEvent outboxEvent = com.cnkart.inventory.model.OutboxEvent.builder()
                    .aggregateType("Inventory")
                    .aggregateId(key)
                    .eventType(event.getClass().getSimpleName())
                    .payload(objectMapper.writeValueAsString(event))
                    .status("PENDING")
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
            outboxEventRepository.save(outboxEvent);
        } catch (Exception exception) {
            log.warn("Unable to save outbox event {} for key {}: {}", event.getClass().getSimpleName(), key, exception.getMessage());
        }
    }
}
