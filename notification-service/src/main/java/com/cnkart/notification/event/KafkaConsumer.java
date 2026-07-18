package com.cnkart.notification.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "cnkart.order.events")
    public void consumeOrderEvents(String message) {
        log.info("Received order event: {}", message);
    }

    @KafkaListener(topics = "cnkart.inventory.events")
    public void consumeInventoryEvents(String message) {
        log.info("Received inventory event: {}", message);
    }
}
