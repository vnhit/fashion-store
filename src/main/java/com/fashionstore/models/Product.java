package com.fashionstore.models;

import java.math.BigDecimal;
import java.util.List;

public class Product {
    private int id;
    private String name;
    private String nameVn;
    private String description;
    private String descriptionVn;
    private String brand;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int discountPercent;
    private int categoryId;
    private String imagePath;
    private double rating;
    private int reviewCount;
    private String badge;
    private String gender;
    private String productType;
    private List<String> colors;
    private List<String> sizes;
    
    public Product() {}
    
    public Product(int id, String name, String nameVn, String brand, BigDecimal price, 
                   BigDecimal originalPrice, int discountPercent, double rating, 
                   int reviewCount, String badge) {
        this.id = id;
        this.name = name;
        this.nameVn = nameVn;
        this.brand = brand;
        this.price = price;
        this.originalPrice = originalPrice;
        this.discountPercent = discountPercent;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.badge = badge;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getNameVn() { return nameVn; }
    public void setNameVn(String nameVn) { this.nameVn = nameVn; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getDescriptionVn() { return descriptionVn; }
    public void setDescriptionVn(String descriptionVn) { this.descriptionVn = descriptionVn; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    
    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }
    
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    
    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    
    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }
    
    public List<String> getSizes() { return sizes; }
    public void setSizes(List<String> sizes) { this.sizes = sizes; }
}

























