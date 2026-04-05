package com.ywtong.springboothtml.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "PRODUCT")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "farmer_id")
    private User farmer;
    
    private String name;
    private String description;
    private String category; // 分类：蔬菜、水果、粮油等
    private BigDecimal price;
    private Integer stock;
    private String unit; // 单位：斤、个等
    private String images; // 图片URL，多个用逗号分隔
    private Boolean isOnSale;
    private Integer viewCount;
    private Integer salesCount;
    private Date createTime;
    private Date updateTime;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getFarmer() {
        return farmer;
    }
    
    public void setFarmer(User farmer) {
        this.farmer = farmer;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public String getImages() {
        return images;
    }
    
    public void setImages(String images) {
        this.images = images;
    }
    
    public Boolean getIsOnSale() {
        return isOnSale;
    }
    
    public void setIsOnSale(Boolean isOnSale) {
        this.isOnSale = isOnSale;
    }
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    public Integer getSalesCount() {
        return salesCount;
    }
    
    public void setSalesCount(Integer salesCount) {
        this.salesCount = salesCount;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public Date getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
} 