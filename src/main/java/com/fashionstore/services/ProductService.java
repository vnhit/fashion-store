package com.fashionstore.services;

import com.fashionstore.dao.ProductDAO;
import com.fashionstore.models.Product;
import java.math.BigDecimal;
import java.util.List;

public class ProductService {
    private ProductDAO productDAO = new ProductDAO();
    
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }
    
    public List<Product> getProductsByCategory(int categoryId) {
        return productDAO.getProductsByCategory(categoryId);
    }
    
    public List<Product> getFeaturedProducts() {
        return productDAO.getFeaturedProducts();
    }
    
    public Product getProductById(int id) {
        return productDAO.getProductById(id);
    }
    
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productDAO.searchProducts(keyword.trim());
    }
    
    public List<Product> filterProducts(Integer categoryId, String brand, BigDecimal minPrice, BigDecimal maxPrice, String sortBy) {
        return productDAO.filterProducts(categoryId, brand, minPrice, maxPrice, sortBy);
    }
    
    public List<String> getAllBrands() {
        return productDAO.getAllBrands();
    }
}




























