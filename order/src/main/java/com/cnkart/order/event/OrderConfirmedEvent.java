package com.cnkart.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderConfirmedEvent {
    private String orderReference;
    private String idempotencyKey;
    private String skuCode;
    private Integer quantity;
    private String status;
    private String message;
}
