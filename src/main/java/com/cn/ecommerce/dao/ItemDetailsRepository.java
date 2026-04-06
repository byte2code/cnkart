package com.cn.ecommerce.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cn.ecommerce.entity.ItemDetails;

public interface ItemDetailsRepository extends JpaRepository<ItemDetails,Integer> {
	
	//derived query
	List<ItemDetails> findByPriceGreaterThan(double price);

	//JPQL
	List<ItemDetails> findByCategoryOrderByPrice(String category);
}