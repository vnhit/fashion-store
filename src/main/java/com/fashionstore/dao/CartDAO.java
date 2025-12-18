package com.fashionstore.dao;

import com.fashionstore.database.DatabaseConnection;
import com.fashionstore.models.CartItem;
import com.fashionstore.models.Product;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {
    private ProductDAO productDAO = new ProductDAO();
    
    public List<CartItem> getCartItemsByUserId(int userId) {
        List<CartItem> cartItems = new ArrayList<>();
        String sql = "SELECT * FROM cart WHERE user_id = ? ORDER BY added_at DESC";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, userId);
                    ResultSet rs = pstmt.executeQuery();
                    
                    while (rs.next()) {
                        CartItem item = new CartItem();
                        item.setId(rs.getInt("id"));
                        item.setUserId(rs.getInt("user_id"));
                        
                        Product product = productDAO.getProductById(rs.getInt("product_id"));
                        item.setProduct(product);
                        item.setSize(rs.getString("size"));
                        item.setColor(rs.getString("color"));
                        item.setQuantity(rs.getInt("quantity"));
                        item.calculateSubtotal();
                        
                        cartItems.add(item);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching cart items: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return cartItems;
    }
    
    public boolean addToCart(int userId, int productId, String size, String color, int quantity) {
        // Check if item already exists
        String checkSql = "SELECT id, quantity FROM cart WHERE user_id = ? AND product_id = ? AND size = ? AND color = ?";
        String insertSql = "INSERT INTO cart (user_id, product_id, size, color, quantity) VALUES (?, ?, ?, ?, ?)";
        String updateSql = "UPDATE cart SET quantity = quantity + ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check existing
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, productId);
                checkStmt.setString(3, size);
                checkStmt.setString(4, color);
                
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    // Update quantity
                    int cartId = rs.getInt("id");
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, quantity);
                        updateStmt.setInt(2, cartId);
                        return updateStmt.executeUpdate() > 0;
                    }
                } else {
                    // Insert new
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setInt(2, productId);
                        insertStmt.setString(3, size);
                        insertStmt.setString(4, color);
                        insertStmt.setInt(5, quantity);
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding to cart: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateCartItemQuantity(int cartId, int quantity) {
        String sql = "UPDATE cart SET quantity = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, cartId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating cart quantity: " + e.getMessage());
            return false;
        }
    }
    
    public boolean removeFromCart(int cartId) {
        String sql = "DELETE FROM cart WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, cartId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing from cart: " + e.getMessage());
            return false;
        }
    }
    
    public boolean clearCart(int userId) {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error clearing cart: " + e.getMessage());
            return false;
        }
    }
    
    public int getCartItemCount(int userId) {
        String sql = "SELECT COUNT(*) as count FROM cart WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting cart count: " + e.getMessage());
        }
        
        return 0;
    }
    
    public BigDecimal getCartTotal(int userId) {
        String sql = "SELECT SUM(p.price * c.quantity) as total FROM cart c " +
                     "JOIN products p ON c.product_id = p.id WHERE c.user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating cart total: " + e.getMessage());
        }
        
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getCartOriginalTotal(int userId) {
        String sql = "SELECT SUM(COALESCE(p.original_price, p.price) * c.quantity) as total FROM cart c " +
                     "JOIN products p ON c.product_id = p.id WHERE c.user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating cart original total: " + e.getMessage());
        }
        
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalCartValueAllUsers() {
        String sql = "SELECT SUM(p.price * c.quantity) as total FROM cart c " +
                     "JOIN products p ON c.product_id = p.id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total cart value: " + e.getMessage());
        }
        
        return BigDecimal.ZERO;
    }
}

