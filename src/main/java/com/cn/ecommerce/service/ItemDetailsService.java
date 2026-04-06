package com.cn.ecommerce.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cn.ecommerce.dao.ItemDetailsRepository;
import com.cn.ecommerce.entity.ItemDetails;

@Service
public class ItemDetailsService {

    @Autowired
    ItemDetailsRepository itemDetailsRepository;

    public void delete(int id) {
        itemDetailsRepository.deleteById(id);
    }

	public List<ItemDetails> getItemDetailsForPrice(double price) {
		
		return itemDetailsRepository.findByPriceGreaterThan(price);
	}

	public List<ItemDetails> getItemDetailsForCategory(String category) {
		return itemDetailsRepository.findByCategoryOrderByPrice(category);
	}

}