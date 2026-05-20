package com.cnkart.order.dto;

import com.cnkart.order.model.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private String orderReference;
    private String idempotencyKey;
    private OrderStatus status;
    private String message;
}
