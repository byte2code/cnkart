package com.cn.ecommerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cn.ecommerce.entity.ItemDetails;
import com.cn.ecommerce.service.ItemDetailsService;

@RestController
@RequestMapping("/api/details")
public class ItemDetailsController {

    @Autowired
    ItemDetailsService itemDetailsService;

    @DeleteMapping("/item/{id}")
    public String delete(@PathVariable int id) {
        itemDetailsService.delete(id);
        return "Item Details has been deleted with id:" + id;
    }
    
    @GetMapping("/item/price/{price}")
    public List<ItemDetails> getItemDetailsForPrice(@PathVariable double price) {
       return itemDetailsService.getItemDetailsForPrice(price);
    }

    @GetMapping("/item/category/{category}")
    public List<ItemDetails> getItemDetailsForCategory(@PathVariable String category) {
       return itemDetailsService.getItemDetailsForCategory(category);
    }
    
}
