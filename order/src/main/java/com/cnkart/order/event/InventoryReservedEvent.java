package com.cnkart.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReservedEvent {
    private String orderReference;
    private String skuCode;
    private Integer requestedQuantity;
    private Integer availableQuantity;
    private String status;
    private String message;
}
