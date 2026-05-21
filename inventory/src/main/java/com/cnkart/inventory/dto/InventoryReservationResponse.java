package com.cnkart.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReservationResponse {
    private String orderReference;
    private String skuCode;
    private Integer requestedQuantity;
    private Integer availableQuantity;
    private boolean reserved;
    private String message;
}
