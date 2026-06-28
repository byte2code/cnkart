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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order", description = "Place and track orders with idempotency support")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackPlaceOrder")
    @Operation(summary = "Place an order", description = "Creates a new order, reserves inventory via the Inventory Service, and returns the order status. Duplicate requests with the same idempotency key return the existing order.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created and processed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public OrderResponse placeOrder(@RequestBody OrderRequest orderRequest) {
        log.info("Placing Order");
        return orderService.placeOrder(orderRequest);
    }
    
    public OrderResponse fallbackPlaceOrder(OrderRequest orderRequest, Throwable t) {
        log.error("Fallback for order placement triggered due to: {}", t.getMessage());
        return new OrderResponse(null, orderRequest.getIdempotencyKey(), OrderStatus.FAILED, "Order service is not available");
    }
}
