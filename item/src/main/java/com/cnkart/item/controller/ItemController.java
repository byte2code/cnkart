package com.cnkart.item.controller;

import com.cnkart.item.dto.ItemRequest;
import com.cnkart.item.dto.ItemResponse;
import com.cnkart.item.service.ItemService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
@Tag(name = "Item", description = "Manage catalog items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an item", description = "Adds a new item to the catalog")
    public void createItem(@RequestBody ItemRequest productRequest) {
        itemService.createItem(productRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all items", description = "Retrieves the full list of available items")
    public List<ItemResponse> getAllItems() {
        return itemService.getAllItems();
    }

}
