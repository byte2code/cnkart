package com.cnkart.order.event;

import com.cnkart.order.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private static final String TOPIC = "cnkart.order.events";

    private final KafkaTemplate<String, String> kafkaTemplate;
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

    private void publish(Object event, String key) {
        try {
            kafkaTemplate.send(TOPIC, key, objectMapper.writeValueAsString(event));
        } catch (Exception exception) {
            log.warn("Unable to publish order event {} for key {}: {}", event.getClass().getSimpleName(), key, exception.getMessage());
        }
    }
}
