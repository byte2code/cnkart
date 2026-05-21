package com.cnkart.inventory.service;

import com.cnkart.inventory.dto.InventoryReservationRequest;
import com.cnkart.inventory.dto.InventoryReservationResponse;
import com.cnkart.inventory.model.Inventory;
import com.cnkart.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void reserveStockDeductsQuantityWhenInventoryIsAvailable() {
        Inventory inventory = new Inventory();
        inventory.setSkuCode("1");
        inventory.setQuantity(10);
        when(inventoryRepository.findBySkuCodeForUpdate("1")).thenReturn(Optional.of(inventory));

        InventoryReservationResponse response = inventoryService.reserveStock(createRequest(2));

        assertThat(response.isReserved()).isTrue();
        assertThat(response.getAvailableQuantity()).isEqualTo(8);
        assertThat(response.getMessage()).isEqualTo("Inventory reserved successfully");
        assertThat(inventory.getQuantity()).isEqualTo(8);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void reserveStockDoesNotDeductQuantityWhenInventoryIsInsufficient() {
        Inventory inventory = new Inventory();
        inventory.setSkuCode("1");
        inventory.setQuantity(1);
        when(inventoryRepository.findBySkuCodeForUpdate("1")).thenReturn(Optional.of(inventory));

        InventoryReservationResponse response = inventoryService.reserveStock(createRequest(2));

        assertThat(response.isReserved()).isFalse();
        assertThat(response.getAvailableQuantity()).isEqualTo(1);
        assertThat(response.getMessage()).isEqualTo("Insufficient stock available for reservation");
        assertThat(inventory.getQuantity()).isEqualTo(1);
        verify(inventoryRepository, never()).save(inventory);
    }

    @Test
    void reserveStockRejectsMissingInventoryRecord() {
        when(inventoryRepository.findBySkuCodeForUpdate("1")).thenReturn(Optional.empty());

        InventoryReservationResponse response = inventoryService.reserveStock(createRequest(2));

        assertThat(response.isReserved()).isFalse();
        assertThat(response.getAvailableQuantity()).isZero();
        assertThat(response.getMessage()).isEqualTo("Inventory record not found for SKU");
        verify(inventoryRepository, never()).save(org.mockito.ArgumentMatchers.any(Inventory.class));
    }

    @Test
    void reserveStockRejectsInvalidQuantity() {
        InventoryReservationResponse response = inventoryService.reserveStock(createRequest(0));

        assertThat(response.isReserved()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Reservation quantity must be greater than zero");
        verify(inventoryRepository, never()).findBySkuCodeForUpdate("1");
    }

    private InventoryReservationRequest createRequest(Integer quantity) {
        return new InventoryReservationRequest("ORD-test", "1", quantity);
    }
}
