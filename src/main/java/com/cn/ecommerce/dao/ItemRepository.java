package com.cn.ecommerce.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cn.ecommerce.entity.Item;

public interface ItemRepository extends JpaRepository<Item,Integer> {
	
	@Query(name="Item.getItemByDesc",nativeQuery=true)
	List<Item> getItemByDesc(String desc);
}
