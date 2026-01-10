package com.fashionstore.dao;

import com.fashionstore.database.DatabaseConnection;
import com.fashionstore.models.Notification;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    
    /**
     * Tạo thông báo mới cho user
     */
    public boolean createNotification(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, order_id, message, type, is_read) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, notification.getUserId());
            pstmt.setInt(2, notification.getOrderId());
            pstmt.setString(3, notification.getMessage());
            pstmt.setString(4, notification.getType());
            pstmt.setBoolean(5, notification.isRead());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating notification: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy tất cả thông báo của user, sắp xếp theo thời gian mới nhất
     */
    public List<Notification> getNotificationsByUser(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notifications: " + e.getMessage());
        }
        
        return notifications;
    }
    
    /**
     * Đếm số thông báo chưa đọc của user
     */
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) as count FROM notifications WHERE user_id = ? AND is_read = false";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting unread notifications: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Đánh dấu thông báo là đã đọc
     */
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = true WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, notificationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Đánh dấu tất cả thông báo của user là đã đọc
     */
    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = true WHERE user_id = ? AND is_read = false";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error marking all notifications as read: " + e.getMessage());
            return false;
        }
    }
    
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setOrderId(rs.getInt("order_id"));
        notification.setMessage(rs.getString("message"));
        notification.setType(rs.getString("type"));
        notification.setRead(rs.getBoolean("is_read"));
        if (rs.getTimestamp("created_at") != null) {
            notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return notification;
    }
}

