package com.cn.cnkart.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cn.cnkart.dal.ItemRepository;
import com.cn.cnkart.entity.Item;

@Service
public class ItemService {

	@Autowired
	ItemRepository itemRepository;
	
	public Item getItemById(int id) {
		return itemRepository.findById(id).get();
	}

	
	public void saveItem(Item item) {
		itemRepository.save(item);
	}

	public void delete(int id) {
		itemRepository.deleteById(id);
	}

	public void update(Item updateItem) {
		itemRepository.save(updateItem);
	}

	public List<Item> getItem() {
		List<Item> itemList = new ArrayList<>();
		itemRepository.findAll().forEach(itemList::add);
		return itemList;
	}
	
}