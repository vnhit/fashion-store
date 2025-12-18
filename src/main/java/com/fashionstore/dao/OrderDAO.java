package com.fashionstore.dao;

import com.fashionstore.database.DatabaseConnection;
import com.fashionstore.models.MonthlyRevenue;
import com.fashionstore.models.Order;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.name AS user_name FROM orders o LEFT JOIN users u ON o.user_id = u.id ORDER BY o.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders: " + e.getMessage());
        }
        
        return orders;
    }
    
    /**
     * Lấy danh sách đơn hàng theo user và trạng thái.
     */
    public List<Order> getOrdersByUserAndStatus(int userId, String status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.name AS user_name FROM orders o " +
                     "LEFT JOIN users u ON o.user_id = u.id " +
                     "WHERE o.user_id = ? AND o.status = ? " +
                     "ORDER BY o.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, status);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders by user and status: " + e.getMessage());
        }
        
        return orders;
    }
    
    /**
     * Đếm số lượng đơn hàng theo user và trạng thái.
     */
    public int getOrderCountByUserAndStatus(int userId, String status) {
        String sql = "SELECT COUNT(*) as count FROM orders WHERE user_id = ? AND status = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, status);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting orders by user and status: " + e.getMessage());
        }
        
        return 0;
    }
    
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }
    
    public List<MonthlyRevenue> getMonthlyRevenue() {
        List<MonthlyRevenue> rows = new ArrayList<>();
        // Doanh thu: tổng số tiền khách phải trả (total_amount). Chỉ tính đơn có status "Hoàn thành".
        // Lưu ý: total_amount trong hệ thống hiện đã là số tiền cuối cùng (đã bao gồm ship và đã trừ voucher nếu có),
        // nên không cộng/trừ thêm shipping_fee/discount_amount để tránh double-count.
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, " +
                     "SUM(COALESCE(total_amount,0)) AS total " +
                     "FROM orders WHERE status = 'Hoàn thành' " +
                     "GROUP BY DATE_FORMAT(created_at, '%Y-%m') ORDER BY month DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String month = rs.getString("month");
                BigDecimal total = rs.getBigDecimal("total");
                rows.add(new MonthlyRevenue(month, total != null ? total : BigDecimal.ZERO));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching monthly revenue: " + e.getMessage());
        }
        
        return rows;
    }

    /**
     * Tổng doanh thu toàn hệ thống (chỉ tính đơn có status "Hoàn thành").
     */
    public BigDecimal getTotalRevenue() {
        String sql = "SELECT SUM(COALESCE(total_amount,0)) AS total FROM orders " +
                     "WHERE status = 'Hoàn thành'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total revenue: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
    
    public int createOrder(int userId, BigDecimal totalAmount, BigDecimal shippingFee, BigDecimal discountAmount, String status) {
        String sql = "INSERT INTO orders (user_id, total_amount, shipping_fee, discount_amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, userId);
            pstmt.setBigDecimal(2, totalAmount != null ? totalAmount : BigDecimal.ZERO);
            pstmt.setBigDecimal(3, shippingFee != null ? shippingFee : BigDecimal.ZERO);
            pstmt.setBigDecimal(4, discountAmount != null ? discountAmount : BigDecimal.ZERO);
            pstmt.setString(5, status != null ? status : "Chờ thanh toán");
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
        }
        return -1;
    }
    
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setShippingFee(rs.getBigDecimal("shipping_fee"));
        order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        order.setStatus(rs.getString("status"));
        if (rs.getTimestamp("created_at") != null) {
            order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        try {
            order.setUserName(rs.getString("user_name"));
        } catch (SQLException e) {
            // ignore
        }
        return order;
    }
}

