package com.fashionstore.dao;

import com.fashionstore.database.DatabaseConnection;
import com.fashionstore.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    // 0 = chưa đăng nhập, sẽ được set sau khi đăng nhập thành công
    private static int currentUserId = 0;
    
    public static int getCurrentUserId() {
        return currentUserId;
    }
    
    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }
    
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setAvatarPath(rs.getString("avatar_path"));
                user.setMembershipLevel(rs.getString("membership_level"));
                user.setPoints(rs.getInt("points"));
                if (rs.getDate("date_of_birth") != null) {
                    user.setDateOfBirth(rs.getDate("date_of_birth").toString());
                }
                user.setGender(rs.getString("gender"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, phone = ?, membership_level = ?, points = ?, date_of_birth = ?, gender = ?, avatar_path = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getMembershipLevel());
            pstmt.setInt(5, user.getPoints());
            if (user.getDateOfBirth() != null && !user.getDateOfBirth().isEmpty()) {
                pstmt.setDate(6, Date.valueOf(user.getDateOfBirth()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            pstmt.setString(7, user.getGender());
            pstmt.setString(8, user.getAvatarPath());
            pstmt.setInt(9, user.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setAvatarPath(rs.getString("avatar_path"));
                user.setMembershipLevel(rs.getString("membership_level"));
                user.setPoints(rs.getInt("points"));
                if (rs.getDate("date_of_birth") != null) {
                    user.setDateOfBirth(rs.getDate("date_of_birth").toString());
                }
                user.setGender(rs.getString("gender"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        
        return users;
    }
    
    public boolean updateMembershipLevel(int userId, String level) {
        String sql = "UPDATE users SET membership_level = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, level);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating membership level: " + e.getMessage());
            return false;
        }
    }
}




