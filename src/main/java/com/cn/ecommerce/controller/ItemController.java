package com.cn.ecommerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cn.ecommerce.entity.Item;
import com.cn.ecommerce.service.ItemService;

@RestController
@RequestMapping("/api")
public class ItemController {

    @Autowired
    ItemService itemService;

    @GetMapping(path = "/item/{id}")
    public Item getItemById(@PathVariable int id) {
        return itemService.getItemById(id);
    }

    @GetMapping(path = "/item")
    public List<Item> getItem() {
        return itemService.getItem();
    }

    @PostMapping("/item/save")
    public Item save(@RequestBody Item item) {
        itemService.save(item);
        return item;
    }

    @PutMapping("/item/update")
    public Item update(@RequestBody Item item) {
        itemService.save(item);
        return item;
    }

    @DeleteMapping("/item/{id}")
    public String delete(@PathVariable int id) {
        itemService.delete(id);
        return "Item has been deleted with id:" + id;
    }

    @GetMapping(path = "/item/desc/{desc}")
    public List<Item> getItemByDesc(@PathVariable String desc) {
        return itemService.getItemByDesc(desc);
    }
    
}
