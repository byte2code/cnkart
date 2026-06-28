package com.cnkart.order.listener;

import com.cnkart.order.event.InventoryRejectedEvent;
import com.cnkart.order.event.InventoryReservedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryEventListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "cnkart.inventory.events")
    public void handleInventoryEvent(String eventPayload) {
        try {
            JsonNode node = objectMapper.readTree(eventPayload);
            String status = node.has("status") ? node.get("status").asText() : "";
            
            if ("RESERVED".equals(status) || eventPayload.contains("InventoryReservedEvent")) {
                InventoryReservedEvent event = objectMapper.readValue(eventPayload, InventoryReservedEvent.class);
                log.info("Inventory reserved successfully for order {}: {} units of {}", 
                    event.getOrderReference(), event.getRequestedQuantity(), event.getSkuCode());
            } else if ("REJECTED".equals(status) || eventPayload.contains("InventoryRejectedEvent")) {
                InventoryRejectedEvent event = objectMapper.readValue(eventPayload, InventoryRejectedEvent.class);
                log.warn("Inventory rejected for order {}: {}", 
                    event.getOrderReference(), event.getMessage());
            } else {
                log.info("Received unknown inventory event payload: {}", eventPayload);
            }
        } catch (Exception e) {
            log.error("Failed to deserialize inventory event: {}", e.getMessage());
        }
    }
}
