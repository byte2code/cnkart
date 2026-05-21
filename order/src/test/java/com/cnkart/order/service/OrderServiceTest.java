package com.cnkart.order.service;

import com.cnkart.order.dto.InventoryReservationRequest;
import com.cnkart.order.dto.InventoryReservationResponse;
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
import static org.mockito.ArgumentMatchers.argThat;
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
        when(inventoryService.reserveStock(any(InventoryReservationRequest.class)))
                .thenReturn(new InventoryReservationResponse("ORD-test", "1", 2, 8, true, "Inventory reserved successfully"));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response.getOrderReference()).startsWith("ORD-");
        assertThat(response.getIdempotencyKey()).isEqualTo("order-key-1");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.getMessage()).isEqualTo("Order confirmed after inventory reservation");
        verify(inventoryService).reserveStock(argThat(reservationRequest ->
                reservationRequest.getOrderReference().startsWith("ORD-")
                        && reservationRequest.getSkuCode().equals("1")
                        && reservationRequest.getQuantity().equals(2)
        ));
    }

    @Test
    void placeOrderRejectsOrderWhenInventoryReservationIsDeclined() {
        OrderRequest request = createRequest("order-key-2");
        when(orderRepository.findByIdempotencyKey("order-key-2")).thenReturn(Optional.empty());
        when(inventoryService.reserveStock(any(InventoryReservationRequest.class)))
                .thenReturn(new InventoryReservationResponse("ORD-test", "1", 2, 1, false, "Insufficient stock available for reservation"));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(response.getMessage()).isEqualTo("Insufficient stock available for reservation");
    }

    @Test
    void placeOrderFailsOrderWhenInventoryReservationCallBreaks() {
        OrderRequest request = createRequest("order-key-3");
        when(orderRepository.findByIdempotencyKey("order-key-3")).thenReturn(Optional.empty());
        when(inventoryService.reserveStock(any(InventoryReservationRequest.class))).thenThrow(new RuntimeException("inventory unavailable"));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(response.getMessage()).isEqualTo("Order failed while reserving inventory");
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
        verify(inventoryService, never()).reserveStock(any(InventoryReservationRequest.class));
    }

    private OrderRequest createRequest(String idempotencyKey) {
        return new OrderRequest("1", BigDecimal.valueOf(799), 2, idempotencyKey);
    }
}
