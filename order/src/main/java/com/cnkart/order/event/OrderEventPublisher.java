package com.cnkart.order.event;

import com.cnkart.order.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.cnkart.order.model.OutboxEvent;
import com.cnkart.order.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private static final String TOPIC = "cnkart.order.events";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void publishOrderCreated(Order order) {
        publish(new OrderCreatedEvent(
                order.getOrderReference(),
                order.getIdempotencyKey(),
                order.getSkuCode(),
                order.getQuantity(),
                order.getStatus().name(),
                "Order created and stored in PENDING state"
        ), order.getOrderReference());
    }

    public void publishOrderConfirmed(Order order) {
        publish(new OrderConfirmedEvent(
                order.getOrderReference(),
                order.getIdempotencyKey(),
                order.getSkuCode(),
                order.getQuantity(),
                order.getStatus().name(),
                "Order confirmed after inventory reservation"
        ), order.getOrderReference());
    }

    public void publishOrderRolledBack(Order order, String reason) {
        publish(new OrderRolledBackEvent(
                order.getOrderReference(),
                reason
        ), order.getOrderReference());
    }

    private void publish(Object event, String key) {
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("Order")
                    .aggregateId(key)
                    .eventType(event.getClass().getSimpleName())
                    .payload(objectMapper.writeValueAsString(event))
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();
            outboxEventRepository.save(outboxEvent);
        } catch (Exception exception) {
            log.warn("Unable to save outbox event {} for key {}: {}", event.getClass().getSimpleName(), key, exception.getMessage());
        }
    }
}
