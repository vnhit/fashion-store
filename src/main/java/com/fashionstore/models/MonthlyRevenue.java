package com.fashionstore.models;

import java.math.BigDecimal;

public class MonthlyRevenue {
    private String month; // yyyy-MM
    private BigDecimal total;
    
    public MonthlyRevenue(String month, BigDecimal total) {
        this.month = month;
        this.total = total;
    }
    
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}









