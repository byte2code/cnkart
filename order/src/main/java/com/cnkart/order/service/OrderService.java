package com.cnkart.order.service;


import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cnkart.order.dto.OrderRequest;
import com.cnkart.order.dto.OrderResponse;
import com.cnkart.order.feign.InventoryService;
import com.cnkart.order.model.Order;
import com.cnkart.order.model.OrderStatus;
import com.cnkart.order.repository.OrderRepository;


@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    
    public OrderService(OrderRepository orderRepository, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
    }

    public OrderResponse placeOrder(OrderRequest orderRequest) {
        String idempotencyKey = resolveIdempotencyKey(orderRequest.getIdempotencyKey());

        Order existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existingOrder != null) {
            return toResponse(existingOrder, "Duplicate order request detected, returning existing order status");
        }

        Order order = new Order();
        String orderReference = createOrderReference();
        order.setOrderNumber(orderReference);
        order.setOrderReference(orderReference);
        order.setIdempotencyKey(idempotencyKey);
        order.setStatus(OrderStatus.PENDING);
        order.setPrice(orderRequest.getPrice());
        order.setQuantity(orderRequest.getQuantity());
        order.setSkuCode(orderRequest.getSkuCode());
        orderRepository.save(order);

        try {
            boolean isInStock = inventoryService.isInStock(Long.parseLong(orderRequest.getSkuCode()), orderRequest.getQuantity());

            if (isInStock) {
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                return toResponse(order, "Order confirmed");
            }

            order.setStatus(OrderStatus.REJECTED);
            orderRepository.save(order);
            return toResponse(order, "Order rejected because item is not in stock");
        } catch (RuntimeException exception) {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            return toResponse(order, "Order failed while validating inventory");
        }


    }

    private String resolveIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return "AUTO-" + UUID.randomUUID();
        }
        return idempotencyKey.trim();
    }

    private String createOrderReference() {
        return "ORD-" + UUID.randomUUID();
    }

    private OrderResponse toResponse(Order order, String message) {
        return new OrderResponse(
                order.getOrderReference(),
                order.getIdempotencyKey(),
                order.getStatus(),
                message
        );
    }
}
