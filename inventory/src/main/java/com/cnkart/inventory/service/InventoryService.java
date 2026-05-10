package com.cnkart.inventory.service;

import org.springframework.stereotype.Service;

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
        Inventory inventory = inventoryRepository.findBySkuCode(String.valueOf(skuCode)).get();
        if(inventory.getQuantity()<qty)
        {
            return false;
        }
        return true;
    }
}
