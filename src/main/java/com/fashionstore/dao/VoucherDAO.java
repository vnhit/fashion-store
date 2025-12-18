package com.fashionstore.dao;

import com.fashionstore.database.DatabaseConnection;
import com.fashionstore.models.Voucher;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoucherDAO {
    
    public Voucher getVoucherByCode(String code) {
        String sql = "SELECT * FROM vouchers WHERE code = ?";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, code);
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        return mapResultSetToVoucher(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching voucher by code: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return null;
    }
    
    public List<Voucher> getAllVouchers() {
        List<Voucher> vouchers = new ArrayList<>();
        String sql = "SELECT * FROM vouchers ORDER BY code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                vouchers.add(mapResultSetToVoucher(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching vouchers: " + e.getMessage());
        }
        return vouchers;
    }
    
    public boolean createVoucher(Voucher voucher) {
        String sql = "INSERT INTO vouchers (code, description, description_vn, discount_type, discount_value, min_order_amount, max_discount_amount, usage_limit, used_count, start_date, end_date, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, voucher.getCode());
            pstmt.setString(2, voucher.getDescription());
            pstmt.setString(3, voucher.getDescriptionVn());
            pstmt.setString(4, voucher.getDiscountType().name());
            pstmt.setBigDecimal(5, voucher.getDiscountValue());
            pstmt.setBigDecimal(6, voucher.getMinOrderAmount());
            pstmt.setBigDecimal(7, voucher.getMaxDiscountAmount());
            if (voucher.getUsageLimit() == null) {
                pstmt.setNull(8, Types.INTEGER);
            } else {
                pstmt.setInt(8, voucher.getUsageLimit());
            }
            pstmt.setDate(9, voucher.getStartDate() != null ? Date.valueOf(voucher.getStartDate()) : null);
            pstmt.setDate(10, voucher.getEndDate() != null ? Date.valueOf(voucher.getEndDate()) : null);
            pstmt.setBoolean(11, voucher.isActive());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating voucher: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateVoucher(Voucher voucher) {
        String sql = "UPDATE vouchers SET description = ?, description_vn = ?, discount_type = ?, discount_value = ?, min_order_amount = ?, max_discount_amount = ?, usage_limit = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, voucher.getDescription());
            pstmt.setString(2, voucher.getDescriptionVn());
            pstmt.setString(3, voucher.getDiscountType().name());
            pstmt.setBigDecimal(4, voucher.getDiscountValue());
            pstmt.setBigDecimal(5, voucher.getMinOrderAmount());
            pstmt.setBigDecimal(6, voucher.getMaxDiscountAmount());
            if (voucher.getUsageLimit() == null) {
                pstmt.setNull(7, Types.INTEGER);
            } else {
                pstmt.setInt(7, voucher.getUsageLimit());
            }
            pstmt.setBoolean(8, voucher.isActive());
            pstmt.setInt(9, voucher.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating voucher: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteVoucher(int voucherId) {
        String sql = "DELETE FROM vouchers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, voucherId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting voucher: " + e.getMessage());
            return false;
        }
    }
    
    public List<Voucher> getAllActiveVouchers() {
        List<Voucher> vouchers = new ArrayList<>();
        String sql = "SELECT * FROM vouchers WHERE is_active = TRUE AND start_date <= CURDATE() AND end_date >= CURDATE() ORDER BY code";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    ResultSet rs = pstmt.executeQuery();
                    
                    while (rs.next()) {
                        Voucher voucher = mapResultSetToVoucher(rs);
                        // Only include vouchers that haven't reached usage limit
                        if (voucher.getUsageLimit() == null || voucher.getUsedCount() < voucher.getUsageLimit()) {
                            vouchers.add(voucher);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active vouchers: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return vouchers;
    }
    
    public boolean canUserUseVoucher(int userId, int voucherId) {
        String sql = "SELECT COUNT(*) as count FROM voucher_usage WHERE user_id = ? AND voucher_id = ?";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, userId);
                    pstmt.setInt(2, voucherId);
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        // For now, allow multiple uses. Can be changed to limit per user
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking voucher usage: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return true;
    }
    
    public boolean recordVoucherUsage(int userId, int voucherId) {
        String sql = "INSERT INTO voucher_usage (voucher_id, user_id) VALUES (?, ?)";
        String updateSql = "UPDATE vouchers SET used_count = used_count + 1 WHERE id = ?";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                conn.setAutoCommit(false);
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql);
                     PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    
                    pstmt.setInt(1, voucherId);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                    
                    updateStmt.setInt(1, voucherId);
                    updateStmt.executeUpdate();
                    
                    conn.commit();
                    return true;
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error recording voucher usage: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return false;
    }
    
    private Voucher mapResultSetToVoucher(ResultSet rs) throws SQLException {
        Voucher voucher = new Voucher();
        voucher.setId(rs.getInt("id"));
        voucher.setCode(rs.getString("code"));
        voucher.setDescription(rs.getString("description"));
        voucher.setDescriptionVn(rs.getString("description_vn"));
        
        String discountTypeStr = rs.getString("discount_type");
        voucher.setDiscountType("PERCENTAGE".equals(discountTypeStr) ? 
            Voucher.DiscountType.PERCENTAGE : Voucher.DiscountType.FIXED);
        
        voucher.setDiscountValue(rs.getBigDecimal("discount_value"));
        voucher.setMinOrderAmount(rs.getBigDecimal("min_order_amount"));
        
        BigDecimal maxDiscount = rs.getBigDecimal("max_discount_amount");
        if (rs.wasNull()) {
            voucher.setMaxDiscountAmount(null);
        } else {
            voucher.setMaxDiscountAmount(maxDiscount);
        }
        
        int usageLimit = rs.getInt("usage_limit");
        if (rs.wasNull()) {
            voucher.setUsageLimit(null);
        } else {
            voucher.setUsageLimit(usageLimit);
        }
        
        voucher.setUsedCount(rs.getInt("used_count"));
        
        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            voucher.setStartDate(startDate.toLocalDate());
        }
        
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            voucher.setEndDate(endDate.toLocalDate());
        }
        
        voucher.setActive(rs.getBoolean("is_active"));
        
        return voucher;
    }
}

