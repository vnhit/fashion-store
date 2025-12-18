package com.fashionstore.dao;

import com.fashionstore.database.DatabaseConnection;
import com.fashionstore.models.Review;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {
    
    public boolean addReview(Review review) {
        String sql = "INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE rating = ?, comment = ?, updated_at = CURRENT_TIMESTAMP";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, review.getProductId());
            pstmt.setInt(2, review.getUserId());
            pstmt.setInt(3, review.getRating());
            pstmt.setString(4, review.getComment());
            pstmt.setInt(5, review.getRating());
            pstmt.setString(6, review.getComment());
            
            int rowsAffected = pstmt.executeUpdate();
            
            // Update product rating and review count
            if (rowsAffected > 0) {
                updateProductRating(review.getProductId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding review: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public List<Review> getReviewsByProductId(int productId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.name as user_name " +
                     "FROM reviews r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "WHERE r.product_id = ? " +
                     "ORDER BY r.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Review review = mapResultSetToReview(rs);
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reviews: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reviews;
    }
    
    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.name as user_name, p.name_vn as product_name " +
                     "FROM reviews r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN products p ON r.product_id = p.id " +
                     "ORDER BY r.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Review review = mapResultSetToReview(rs);
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all reviews: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reviews;
    }
    
    /**
     * Lấy tất cả đánh giá của một người dùng (kèm tên sản phẩm).
     */
    public List<Review> getReviewsByUserId(int userId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.name as user_name, p.name_vn as product_name " +
                     "FROM reviews r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN products p ON r.product_id = p.id " +
                     "WHERE r.user_id = ? " +
                     "ORDER BY r.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Review review = mapResultSetToReview(rs);
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reviews by user id: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reviews;
    }
    
    public Review getReviewByUserAndProduct(int userId, int productId) {
        String sql = "SELECT r.*, u.name as user_name " +
                     "FROM reviews r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "WHERE r.user_id = ? AND r.product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToReview(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user review: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean deleteReview(int reviewId) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reviewId);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get product_id before deletion to update rating
                Review review = getReviewById(reviewId);
                if (review != null) {
                    updateProductRating(review.getProductId());
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting review: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    private Review getReviewById(int reviewId) {
        String sql = "SELECT * FROM reviews WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reviewId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToReview(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching review by id: " + e.getMessage());
        }
        
        return null;
    }
    
    private void updateProductRating(int productId) {
        String sql = "UPDATE products SET " +
                     "rating = (SELECT COALESCE(AVG(rating), 0) FROM reviews WHERE product_id = ?), " +
                     "review_count = (SELECT COUNT(*) FROM reviews WHERE product_id = ?) " +
                     "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, productId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating product rating: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getInt("id"));
        review.setProductId(rs.getInt("product_id"));
        review.setUserId(rs.getInt("user_id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            review.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            review.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        try {
            review.setUserName(rs.getString("user_name"));
        } catch (SQLException e) {
            // user_name might not be in all queries
        }
        
        try {
            review.setProductName(rs.getString("product_name"));
        } catch (SQLException e) {
            // product_name might not be in all queries
        }
        
        return review;
    }
    
    public double getAverageRating(int productId) {
        String sql = "SELECT COALESCE(AVG(rating), 0) as avg_rating FROM reviews WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.err.println("Error getting average rating: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    public int getReviewCount(int productId) {
        String sql = "SELECT COUNT(*) as count FROM reviews WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting review count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
}

