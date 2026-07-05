package com.cnkart.order.listener;

import com.cnkart.order.event.OrderRolledBackEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderRolledBackLogger {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "cnkart.order.events")
    public void handleOrderEvent(String eventPayload) {
        try {
            if (eventPayload.contains("OrderRolledBackEvent")) {
                OrderRolledBackEvent event = objectMapper.readValue(eventPayload, OrderRolledBackEvent.class);
                log.info("SAGA AWARENESS: Consumed OrderRolledBackEvent for order {}. Reason: {}", 
                    event.getOrderReference(), event.getReason());
            }
        } catch (Exception e) {
            log.error("Failed to deserialize order event: {}", e.getMessage());
        }
    }
}
