package com.cnkart.order.service;

import com.cnkart.order.dto.OrderRequest;
import com.cnkart.order.dto.OrderResponse;
import com.cnkart.order.feign.InventoryService;
import com.cnkart.order.model.OrderStatus;
import com.cnkart.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrderConfirmsOrderWhenInventoryIsAvailable() {
        OrderRequest request = createRequest("order-key-1");
        when(orderRepository.findByIdempotencyKey("order-key-1")).thenReturn(Optional.empty());
        when(inventoryService.isInStock(1L, 2)).thenReturn(true);

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response.getOrderReference()).startsWith("ORD-");
        assertThat(response.getIdempotencyKey()).isEqualTo("order-key-1");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.getMessage()).isEqualTo("Order confirmed");
    }

    @Test
    void placeOrderRejectsOrderWhenInventoryIsUnavailable() {
        OrderRequest request = createRequest("order-key-2");
        when(orderRepository.findByIdempotencyKey("order-key-2")).thenReturn(Optional.empty());
        when(inventoryService.isInStock(1L, 2)).thenReturn(false);

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(response.getMessage()).isEqualTo("Order rejected because item is not in stock");
    }

    @Test
    void placeOrderFailsOrderWhenInventoryCallBreaks() {
        OrderRequest request = createRequest("order-key-3");
        when(orderRepository.findByIdempotencyKey("order-key-3")).thenReturn(Optional.empty());
        when(inventoryService.isInStock(1L, 2)).thenThrow(new RuntimeException("inventory unavailable"));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(response.getMessage()).isEqualTo("Order failed while validating inventory");
    }

    @Test
    void placeOrderReturnsExistingOrderForDuplicateIdempotencyKey() {
        com.cnkart.order.model.Order existingOrder = new com.cnkart.order.model.Order();
        existingOrder.setOrderReference("ORD-existing");
        existingOrder.setIdempotencyKey("duplicate-key");
        existingOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByIdempotencyKey("duplicate-key")).thenReturn(Optional.of(existingOrder));

        OrderResponse response = orderService.placeOrder(createRequest("duplicate-key"));

        assertThat(response.getOrderReference()).isEqualTo("ORD-existing");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.getMessage()).isEqualTo("Duplicate order request detected, returning existing order status");
        verify(inventoryService, never()).isInStock(any(Long.class), any(Integer.class));
    }

    private OrderRequest createRequest(String idempotencyKey) {
        return new OrderRequest("1", BigDecimal.valueOf(799), 2, idempotencyKey);
    }
}
