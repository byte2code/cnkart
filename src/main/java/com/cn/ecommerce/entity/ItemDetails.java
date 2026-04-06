package com.cn.ecommerce.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity

@NamedQuery(name="ItemDetails.findByCategoryOrderByPrice",
query="Select itd from ItemDetails itd where itd.category=?1 ORDER BY itd.price DESC")

@Table(name = "item_details")
public class ItemDetails {
    @Column
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    @Column
    private String category;
    @Column
    private double price;
    @Column
    private String brand;
    @OneToOne(mappedBy = "itemDetails",cascade = CascadeType.ALL)
    @JsonIgnore
    private Item item;

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
