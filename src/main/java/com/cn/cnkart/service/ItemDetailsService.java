package com.cn.cnkart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cn.cnkart.dal.ItemDetailsRepository;

@Service
public class ItemDetailsService {

	@Autowired
	ItemDetailsRepository itemDetailsRepository;
	
	public void delete(int id) {
		itemDetailsRepository.deleteById(id);
	}


}