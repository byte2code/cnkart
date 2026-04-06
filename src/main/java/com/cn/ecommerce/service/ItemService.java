package com.cn.ecommerce.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cn.ecommerce.dao.ItemRepository;
import com.cn.ecommerce.entity.Item;

@Service
public class ItemService {

    @Autowired
    ItemRepository itemRepository;

    public Item getItemById(int id) {
       return itemRepository.findById(id).get();
    }

    public List<Item> getItem() {
        List<Item> items = new ArrayList<>();
        itemRepository.findAll().forEach(item -> items.add(item));
        return items;
    }

    public void save(Item itemDetails) {
        itemRepository.save(itemDetails);
    }

    public void delete(int id) {
        itemRepository.deleteById(id);
    }

	public List<Item> getItemByDesc(String desc) {
		return itemRepository.getItemByDesc(desc);
	}
}
