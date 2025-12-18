package com.fashionstore.dao;

import com.fashionstore.database.DatabaseConnection;
import com.fashionstore.models.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesDAO {
    private final ProductDAO productDAO = new ProductDAO();

    public boolean addFavorite(int userId, int productId) {
        String sql = "INSERT IGNORE INTO favorites (user_id, product_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding favorite: " + e.getMessage());
            return false;
        }
    }

    public boolean removeFavorite(int userId, int productId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing favorite: " + e.getMessage());
            return false;
        }
    }

    public boolean isFavorite(int userId, int productId) {
        String sql = "SELECT 1 FROM favorites WHERE user_id = ? AND product_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking favorite: " + e.getMessage());
            return false;
        }
    }

    public Set<Integer> getFavoriteProductIds(int userId) {
        Set<Integer> favoriteIds = new HashSet<>();
        String sql = "SELECT product_id FROM favorites WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                favoriteIds.add(rs.getInt("product_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching favorite product ids: " + e.getMessage());
        }

        return favoriteIds;
    }

    public List<Product> getFavoriteProducts(int userId) {
        List<Product> favorites = new ArrayList<>();
        String sql = "SELECT product_id FROM favorites WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Product product = productDAO.getProductById(rs.getInt("product_id"));
                if (product != null) {
                    favorites.add(product);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching favorites: " + e.getMessage());
        }

        return favorites;
    }
}


