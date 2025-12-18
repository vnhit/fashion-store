package com.fashionstore.services;

import com.fashionstore.dao.FavoritesDAO;
import com.fashionstore.models.Product;
import java.util.List;
import java.util.Set;

public class FavoritesService {
    private final FavoritesDAO favoritesDAO = new FavoritesDAO();

    public boolean addFavorite(int userId, int productId) {
        if (userId <= 0 || productId <= 0) {
            return false;
        }
        return favoritesDAO.addFavorite(userId, productId);
    }

    public boolean removeFavorite(int userId, int productId) {
        if (userId <= 0 || productId <= 0) {
            return false;
        }
        return favoritesDAO.removeFavorite(userId, productId);
    }

    public boolean isFavorite(int userId, int productId) {
        if (userId <= 0 || productId <= 0) {
            return false;
        }
        return favoritesDAO.isFavorite(userId, productId);
    }

    public Set<Integer> getFavoriteProductIds(int userId) {
        return favoritesDAO.getFavoriteProductIds(userId);
    }

    public List<Product> getFavorites(int userId) {
        return favoritesDAO.getFavoriteProducts(userId);
    }
}


