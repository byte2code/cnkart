package com.cnkart.order.listener;

import com.cnkart.order.event.InventoryRejectedEvent;
import com.cnkart.order.event.InventoryReservedEvent;
import com.cnkart.order.event.OrderEventPublisher;
import com.cnkart.order.model.Order;
import com.cnkart.order.model.OrderStatus;
import com.cnkart.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryEventListener {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

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
                
                Optional<Order> orderOpt = orderRepository.findByOrderReference(event.getOrderReference());
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    if (order.getStatus() != OrderStatus.REJECTED && order.getStatus() != OrderStatus.CANCELLED) {
                        log.info("SAGA: Compensating action triggered for order {}", order.getOrderReference());
                        order.setStatus(OrderStatus.CANCELLED);
                        orderRepository.save(order);
                        orderEventPublisher.publishOrderRolledBack(order, event.getMessage());
                    }
                }
            } else {
                log.info("Received unknown inventory event payload: {}", eventPayload);
            }
        } catch (Exception e) {
            log.error("Failed to deserialize inventory event: {}", e.getMessage());
        }
    }
}
