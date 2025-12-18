package com.fashionstore.dao;

import com.fashionstore.database.DatabaseConnection;
import com.fashionstore.models.Address;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressDAO {
    
    public List<Address> getAddressesByUserId(int userId) {
        List<Address> addresses = new ArrayList<>();
        String sql = "SELECT * FROM addresses WHERE user_id = ? ORDER BY is_default DESC, created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Address address = new Address();
                address.setId(rs.getInt("id"));
                address.setUserId(rs.getInt("user_id"));
                address.setFullName(rs.getString("full_name"));
                address.setPhone(rs.getString("phone"));
                address.setProvince(rs.getString("province"));
                address.setDistrict(rs.getString("district"));
                address.setWard(rs.getString("ward"));
                address.setStreetAddress(rs.getString("street_address"));
                address.setDefault(rs.getBoolean("is_default"));
                addresses.add(address);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching addresses: " + e.getMessage());
        }
        
        return addresses;
    }
    
    public Address getAddressById(int id) {
        String sql = "SELECT * FROM addresses WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Address address = new Address();
                address.setId(rs.getInt("id"));
                address.setUserId(rs.getInt("user_id"));
                address.setFullName(rs.getString("full_name"));
                address.setPhone(rs.getString("phone"));
                address.setProvince(rs.getString("province"));
                address.setDistrict(rs.getString("district"));
                address.setWard(rs.getString("ward"));
                address.setStreetAddress(rs.getString("street_address"));
                address.setDefault(rs.getBoolean("is_default"));
                return address;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching address: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean addAddress(Address address) {
        String sql = "INSERT INTO addresses (user_id, full_name, phone, province, district, ward, street_address, is_default) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, address.getUserId());
            pstmt.setString(2, address.getFullName());
            pstmt.setString(3, address.getPhone());
            pstmt.setString(4, address.getProvince());
            pstmt.setString(5, address.getDistrict());
            pstmt.setString(6, address.getWard());
            pstmt.setString(7, address.getStreetAddress());
            pstmt.setBoolean(8, address.isDefault());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0 && address.isDefault()) {
                // Nếu địa chỉ này là mặc định, bỏ mặc định của các địa chỉ khác
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newAddressId = generatedKeys.getInt(1);
                        setAsDefaultAddress(conn, address.getUserId(), newAddressId);
                    }
                }
            }
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding address: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateAddress(Address address) {
        String sql = "UPDATE addresses SET full_name = ?, phone = ?, province = ?, district = ?, ward = ?, street_address = ?, is_default = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, address.getFullName());
            pstmt.setString(2, address.getPhone());
            pstmt.setString(3, address.getProvince());
            pstmt.setString(4, address.getDistrict());
            pstmt.setString(5, address.getWard());
            pstmt.setString(6, address.getStreetAddress());
            pstmt.setBoolean(7, address.isDefault());
            pstmt.setInt(8, address.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0 && address.isDefault()) {
                // Nếu địa chỉ này là mặc định, bỏ mặc định của các địa chỉ khác
                setAsDefaultAddress(conn, address.getUserId(), address.getId());
            }
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating address: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteAddress(int id) {
        String sql = "DELETE FROM addresses WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting address: " + e.getMessage());
            return false;
        }
    }
    
    private void setAsDefaultAddress(Connection conn, int userId, int addressId) throws SQLException {
        // Bỏ mặc định của tất cả địa chỉ khác
        String sql1 = "UPDATE addresses SET is_default = FALSE WHERE user_id = ? AND id != ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, addressId);
            pstmt.executeUpdate();
        }
        
        // Cập nhật default_address_id trong users table
        String sql2 = "UPDATE users SET default_address_id = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {
            pstmt.setInt(1, addressId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }
}

