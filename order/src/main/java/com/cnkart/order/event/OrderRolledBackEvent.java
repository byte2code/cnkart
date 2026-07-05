package com.cnkart.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRolledBackEvent {
    private String orderReference;
    private String reason;
}
