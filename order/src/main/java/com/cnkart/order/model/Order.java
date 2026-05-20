package com.cnkart.order.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String orderNumber;
    @Column(unique = true)
    private String orderReference;
    @Column(unique = true)
    private String idempotencyKey;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
}
