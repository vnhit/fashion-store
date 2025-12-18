package com.fashionstore.models;

import java.math.BigDecimal;

public class CartItem {
    private int id;
    private int userId;
    private Product product;
    private String size;
    private String color;
    private int quantity;
    private BigDecimal subtotal;
    
    public CartItem() {}
    
    public CartItem(int id, int userId, Product product, String size, String color, int quantity) {
        this.id = id;
        this.userId = userId;
        this.product = product;
        this.size = size;
        this.color = color;
        this.quantity = quantity;
        calculateSubtotal();
    }
    
    public void calculateSubtotal() {
        if (product != null && product.getPrice() != null) {
            this.subtotal = product.getPrice().multiply(new BigDecimal(quantity));
        }
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public Product getProduct() { return product; }
    public void setProduct(Product product) { 
        this.product = product;
        calculateSubtotal();
    }
    
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        calculateSubtotal();
    }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}




























