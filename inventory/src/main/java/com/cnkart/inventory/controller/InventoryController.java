package com.cnkart.inventory.controller;

import com.cnkart.inventory.dto.InventoryReservationRequest;
import com.cnkart.inventory.dto.InventoryReservationResponse;
import com.cnkart.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory", description = "Check stock availability and reserve inventory for orders")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Check stock availability", description = "Returns true if the requested quantity is available for the given SKU code.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock availability check completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public boolean isInStock(
            @Parameter(description = "SKU code of the item to check") @RequestParam Long skuCode,
            @Parameter(description = "Quantity to check availability for") @RequestParam Integer qty) {
        log.info("Received inventory check request for skuCode: {}", skuCode);
        return inventoryService.isInStock(skuCode, qty);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reserve inventory for an order", description = "Reserves the requested quantity for an order reference. Uses pessimistic locking to prevent overselling during concurrent requests.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reservation processed (check response body for reserved status)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public InventoryReservationResponse reserveStock(@RequestBody InventoryReservationRequest request) {
        log.info("Received inventory reservation request for orderReference: {}", request.getOrderReference());
        return inventoryService.reserveStock(request);
    }
}
