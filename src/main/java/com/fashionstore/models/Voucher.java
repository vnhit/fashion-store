package com.fashionstore.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Voucher {
    private int id;
    private String code;
    private String description;
    private String descriptionVn;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private int usedCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
    
    public enum DiscountType {
        PERCENTAGE,
        FIXED
    }
    
    public Voucher() {}
    
    public Voucher(int id, String code, String descriptionVn, DiscountType discountType, 
                   BigDecimal discountValue, BigDecimal minOrderAmount, BigDecimal maxDiscountAmount,
                   Integer usageLimit, int usedCount, LocalDate startDate, LocalDate endDate, boolean isActive) {
        this.id = id;
        this.code = code;
        this.descriptionVn = descriptionVn;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.maxDiscountAmount = maxDiscountAmount;
        this.usageLimit = usageLimit;
        this.usedCount = usedCount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }
    
    // Calculate discount amount based on order total
    public BigDecimal calculateDiscount(BigDecimal orderTotal) {
        if (orderTotal.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discount = BigDecimal.ZERO;
        
        if (discountType == DiscountType.PERCENTAGE) {
            discount = orderTotal.multiply(discountValue).divide(new BigDecimal(100));
            if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                discount = maxDiscountAmount;
            }
        } else if (discountType == DiscountType.FIXED) {
            discount = discountValue;
            if (discount.compareTo(orderTotal) > 0) {
                discount = orderTotal;
            }
        }
        return discount;
    }
    
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return isActive && 
               !today.isBefore(startDate) && 
               !today.isAfter(endDate) &&
               (usageLimit == null || usedCount < usageLimit);
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getDescriptionVn() { return descriptionVn; }
    public void setDescriptionVn(String descriptionVn) { this.descriptionVn = descriptionVn; }
    
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    
    public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
    
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    
    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}

