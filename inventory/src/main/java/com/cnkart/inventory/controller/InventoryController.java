package com.cnkart.inventory.controller;

import com.cnkart.inventory.dto.InventoryReservationRequest;
import com.cnkart.inventory.dto.InventoryReservationResponse;
import com.cnkart.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@RequestParam Long skuCode,@RequestParam Integer qty) {
        log.info("Received inventory check request for skuCode: {}", skuCode);
        return inventoryService.isInStock(skuCode,qty);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryReservationResponse reserveStock(@RequestBody InventoryReservationRequest request) {
        log.info("Received inventory reservation request for orderReference: {}", request.getOrderReference());
        return inventoryService.reserveStock(request);
    }
}
