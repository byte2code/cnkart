package com.cnkart.inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cnkart.inventory.dto.InventoryReservationRequest;
import com.cnkart.inventory.dto.InventoryReservationResponse;
import com.cnkart.inventory.model.Inventory;
import com.cnkart.inventory.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public boolean isInStock(Long skuCode,Integer qty) {
        log.info("Checking Inventory");
        return inventoryRepository.findBySkuCode(String.valueOf(skuCode))
                .map(inventory -> inventory.getQuantity() >= qty)
                .orElse(false);
    }

    @Transactional
    public InventoryReservationResponse reserveStock(InventoryReservationRequest request) {
        log.info("Reserving inventory for orderReference: {}", request.getOrderReference());

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            return new InventoryReservationResponse(
                    request.getOrderReference(),
                    request.getSkuCode(),
                    request.getQuantity(),
                    null,
                    false,
                    "Reservation quantity must be greater than zero"
            );
        }

        Inventory inventory = inventoryRepository.findBySkuCodeForUpdate(request.getSkuCode()).orElse(null);
        if (inventory == null) {
            return new InventoryReservationResponse(
                    request.getOrderReference(),
                    request.getSkuCode(),
                    request.getQuantity(),
                    0,
                    false,
                    "Inventory record not found for SKU"
            );
        }

        Integer availableQuantity = inventory.getQuantity();
        if (availableQuantity < request.getQuantity()) {
            return new InventoryReservationResponse(
                    request.getOrderReference(),
                    request.getSkuCode(),
                    request.getQuantity(),
                    availableQuantity,
                    false,
                    "Insufficient stock available for reservation"
            );
        }

        inventory.setQuantity(availableQuantity - request.getQuantity());
        inventoryRepository.save(inventory);

        return new InventoryReservationResponse(
                request.getOrderReference(),
                request.getSkuCode(),
                request.getQuantity(),
                inventory.getQuantity(),
                true,
                "Inventory reserved successfully"
        );
    }
}
