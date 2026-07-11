package com.cnkart.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReservationRequest {
    @NotBlank(message = "Order Reference is mandatory")
    private String orderReference;

    @NotBlank(message = "SKU Code is mandatory")
    private String skuCode;

    @NotNull(message = "Quantity is mandatory")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
