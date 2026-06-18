package com.cnkart.order.controller;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cnkart.order.dto.OrderRequest;
import com.cnkart.order.dto.OrderResponse;
import com.cnkart.order.model.OrderStatus;
import com.cnkart.order.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackPlaceOrder")
    public OrderResponse placeOrder(@RequestBody OrderRequest orderRequest) {
        log.info("Placing Order");
        return orderService.placeOrder(orderRequest);
    }
    
    public OrderResponse fallbackPlaceOrder(OrderRequest orderRequest, Throwable t) {
        log.error("Fallback for order placement triggered due to: {}", t.getMessage());
        return new OrderResponse(null, orderRequest.getIdempotencyKey(), OrderStatus.FAILED, "Order service is not available");
    }
}
