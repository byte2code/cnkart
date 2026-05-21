package com.cnkart.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReservationRequest {
    private String orderReference;
    private String skuCode;
    private Integer quantity;
}
