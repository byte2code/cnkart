package com.cnkart.order.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryEventListener {

    @KafkaListener(topics = "cnkart.inventory.events")
    public void handleInventoryEvent(String eventPayload) {
        // Stub implementation to close the event loop
        log.info("Received inventory event payload: {}", eventPayload);
        // In a real implementation, we would deserialize this payload and update the order state.
    }
}
