package com.fashionstore.services;

import com.fashionstore.dao.CartDAO;
import com.fashionstore.models.CartItem;
import java.math.BigDecimal;
import java.util.List;

public class CartService {
    private CartDAO cartDAO = new CartDAO();
    
    public List<CartItem> getCartItems(int userId) {
        return cartDAO.getCartItemsByUserId(userId);
    }
    
    public boolean addToCart(int userId, int productId, String size, String color, int quantity) {
        return cartDAO.addToCart(userId, productId, size, color, quantity);
    }
    
    public boolean updateQuantity(int cartId, int quantity) {
        if (quantity <= 0) {
            return removeFromCart(cartId);
        }
        return cartDAO.updateCartItemQuantity(cartId, quantity);
    }
    
    public boolean removeFromCart(int cartId) {
        return cartDAO.removeFromCart(cartId);
    }
    
    public boolean clearCart(int userId) {
        return cartDAO.clearCart(userId);
    }
    
    public int getCartItemCount(int userId) {
        return cartDAO.getCartItemCount(userId);
    }
    
    public BigDecimal getCartTotal(int userId) {
        return cartDAO.getCartTotal(userId);
    }
    
    public BigDecimal getCartOriginalTotal(int userId) {
        return cartDAO.getCartOriginalTotal(userId);
    }
    
    public BigDecimal calculateShippingFee(BigDecimal total) {
        // Free shipping for orders over 500,000
        if (total.compareTo(new BigDecimal(500000)) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(30000);
    }
    
    public BigDecimal calculateFinalTotal(int userId, BigDecimal discountAmount) {
        BigDecimal subtotal = getCartTotal(userId);
        BigDecimal shipping = calculateShippingFee(subtotal);
        return subtotal.add(shipping).subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }
}



















