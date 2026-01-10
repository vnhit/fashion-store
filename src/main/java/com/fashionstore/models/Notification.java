package com.fashionstore.models;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int userId;
    private int orderId;
    private String message;
    private String type; // "ORDER_SHIPPED", "ORDER_DELIVERED"
    private boolean isRead;
    private LocalDateTime createdAt;
    
    public Notification() {
    }
    
    public Notification(int userId, int orderId, String message, String type) {
        this.userId = userId;
        this.orderId = orderId;
        this.message = message;
        this.type = type;
        this.isRead = false;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean isRead) { this.isRead = isRead; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
