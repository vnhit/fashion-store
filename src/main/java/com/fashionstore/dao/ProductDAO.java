package com.fashionstore.dao;

import com.fashionstore.database.DatabaseConnection;
import com.fashionstore.models.Product;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY created_at DESC";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    while (rs.next()) {
                        Product product = mapResultSetToProduct(rs);
                        product.setColors(getProductColors(product.getId()));
                        product.setSizes(getProductSizes(product.getId()));
                        products.add(product);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching products: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return products;
    }
    
    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                product.setColors(getProductColors(product.getId()));
                product.setSizes(getProductSizes(product.getId()));
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching products by category: " + e.getMessage());
        }
        
        return products;
    }
    
    public List<Product> getFeaturedProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE rating >= 4.5 ORDER BY rating DESC LIMIT 8";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    while (rs.next()) {
                        Product product = mapResultSetToProduct(rs);
                        product.setColors(getProductColors(product.getId()));
                        product.setSizes(getProductSizes(product.getId()));
                        products.add(product);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching featured products: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return products;
    }
    
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, name_vn = ?, description = ?, description_vn = ?, " +
                     "brand = ?, price = ?, original_price = ?, discount_percent = ?, category_id = ?, " +
                     "image_path = ?, badge = ?, gender = ?, product_type = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getNameVn());
            pstmt.setString(3, product.getDescription());
            pstmt.setString(4, product.getDescriptionVn());
            pstmt.setString(5, product.getBrand());
            pstmt.setBigDecimal(6, product.getPrice());
            if (product.getOriginalPrice() != null) {
                pstmt.setBigDecimal(7, product.getOriginalPrice());
            } else {
                pstmt.setNull(7, Types.DECIMAL);
            }
            pstmt.setInt(8, product.getDiscountPercent());
            pstmt.setInt(9, product.getCategoryId());
            pstmt.setString(10, product.getImagePath());
            pstmt.setString(11, product.getBadge());
            pstmt.setString(12, product.getGender());
            pstmt.setString(13, product.getProductType());
            pstmt.setInt(14, product.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }
    
    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name_vn LIKE ? OR description_vn LIKE ? OR brand LIKE ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                product.setColors(getProductColors(product.getId()));
                product.setSizes(getProductSizes(product.getId()));
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println("Error searching products: " + e.getMessage());
        }
        
        return products;
    }
    
    public List<Product> filterProducts(Integer categoryId, String brand, BigDecimal minPrice, BigDecimal maxPrice, String sortBy) {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        
        if (brand != null && !brand.isEmpty()) {
            sql.append(" AND brand = ?");
            params.add(brand);
        }
        
        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }
        
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }
        
        // Sort
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy) {
                case "price_asc":
                    sql.append(" ORDER BY price ASC");
                    break;
                case "price_desc":
                    sql.append(" ORDER BY price DESC");
                    break;
                case "rating_desc":
                    sql.append(" ORDER BY rating DESC");
                    break;
                case "rating_asc":
                    sql.append(" ORDER BY rating ASC");
                    break;
                case "newest":
                    sql.append(" ORDER BY created_at DESC");
                    break;
                default:
                    sql.append(" ORDER BY created_at DESC");
            }
        } else {
            sql.append(" ORDER BY created_at DESC");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof BigDecimal) {
                    pstmt.setBigDecimal(i + 1, (BigDecimal) param);
                }
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                product.setColors(getProductColors(product.getId()));
                product.setSizes(getProductSizes(product.getId()));
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println("Error filtering products: " + e.getMessage());
        }
        
        return products;
    }
    
    public List<String> getAllBrands() {
        List<String> brands = new ArrayList<>();
        String sql = "SELECT DISTINCT brand FROM products WHERE brand IS NOT NULL ORDER BY brand";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    while (rs.next()) {
                        brands.add(rs.getString("brand"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching brands: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return brands;
    }
    
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                product.setColors(getProductColors(product.getId()));
                product.setSizes(getProductSizes(product.getId()));
                return product;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product by id: " + e.getMessage());
        }
        
        return null;
    }
    
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setNameVn(rs.getString("name_vn"));
        product.setDescription(rs.getString("description"));
        product.setDescriptionVn(rs.getString("description_vn"));
        product.setBrand(rs.getString("brand"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setOriginalPrice(rs.getBigDecimal("original_price"));
        product.setDiscountPercent(rs.getInt("discount_percent"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setImagePath(rs.getString("image_path"));
        
        // Tính rating từ reviews thực tế, không dùng giá trị mặc định
        double ratingFromReviews = getAverageRatingFromReviews(product.getId());
        int reviewCount = getReviewCountFromReviews(product.getId());
        
        // Chỉ dùng rating từ database nếu không có reviews nào
        if (reviewCount > 0) {
            product.setRating(ratingFromReviews);
        } else {
            product.setRating(rs.getDouble("rating"));
        }
        product.setReviewCount(reviewCount);
        product.setBadge(rs.getString("badge"));
        try {
            product.setGender(rs.getString("gender"));
        } catch (SQLException e) {
            // Column might not exist yet
            product.setGender(null);
        }
        try {
            product.setProductType(rs.getString("product_type"));
        } catch (SQLException e) {
            // Column might not exist yet
            product.setProductType(null);
        }
        return product;
    }
    
    private double getAverageRatingFromReviews(int productId) {
        String sql = "SELECT COALESCE(AVG(rating), 0) as avg_rating FROM reviews WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.err.println("Error getting average rating from reviews: " + e.getMessage());
        }
        
        return 0.0;
    }
    
    private int getReviewCountFromReviews(int productId) {
        String sql = "SELECT COUNT(*) as count FROM reviews WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting review count from reviews: " + e.getMessage());
        }
        
        return 0;
    }
    
    private List<String> getProductColors(int productId) {
        List<String> colors = new ArrayList<>();
        String sql = "SELECT color_code FROM product_colors WHERE product_id = ?";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, productId);
                    ResultSet rs = pstmt.executeQuery();
                    
                    while (rs.next()) {
                        colors.add(rs.getString("color_code"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product colors: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return colors;
    }
    
    private List<String> getProductSizes(int productId) {
        List<String> sizes = new ArrayList<>();
        String sql = "SELECT DISTINCT size FROM product_sizes WHERE product_id = ?";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, productId);
                    ResultSet rs = pstmt.executeQuery();
                    
                    while (rs.next()) {
                        sizes.add(rs.getString("size"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product sizes: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        // Sắp xếp size theo thứ tự từ nhỏ đến lớn
        return sortSizes(sizes);
    }
    
    /**
     * Sắp xếp danh sách size theo thứ tự từ nhỏ đến lớn: XS, S, M, L, XL, XXL, XXXL
     */
    public static List<String> sortSizes(List<String> sizes) {
        if (sizes == null || sizes.isEmpty()) {
            return sizes;
        }
        
        // Định nghĩa thứ tự size
        java.util.Map<String, Integer> sizeOrder = new java.util.HashMap<>();
        sizeOrder.put("XS", 1);
        sizeOrder.put("S", 2);
        sizeOrder.put("M", 3);
        sizeOrder.put("L", 4);
        sizeOrder.put("XL", 5);
        sizeOrder.put("XXL", 6);
        sizeOrder.put("XXXL", 7);
        
        // Sắp xếp theo thứ tự đã định nghĩa
        sizes.sort((s1, s2) -> {
            Integer order1 = sizeOrder.getOrDefault(s1.toUpperCase().trim(), 999);
            Integer order2 = sizeOrder.getOrDefault(s2.toUpperCase().trim(), 999);
            int compare = order1.compareTo(order2);
            // Nếu cùng thứ tự (không có trong map), sắp xếp theo alphabet
            if (compare == 0) {
                return s1.compareToIgnoreCase(s2);
            }
            return compare;
        });
        
        return sizes;
    }
    
    public String getColorNameByCode(int productId, String colorCode) {
        String sql = "SELECT color_name FROM product_colors WHERE product_id = ? AND LOWER(color_code) = LOWER(?)";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, productId);
                    pstmt.setString(2, colorCode);
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        String colorName = rs.getString("color_name");
                        return colorName;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching color name: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        // Fallback: map common color codes to human-friendly names
        if (colorCode != null) {
            String codeLower = colorCode.toLowerCase();
            switch (codeLower) {
                case "#1e3a5f":
                    return "Navy Blue";
                case "#ffffff":
                    return "White";
                case "#e0e0e0":
                    return "Light Gray";
                case "#8b4513":
                    return "Brown";
                default:
                    return "Unknown color";
            }
        }
        
        return "Unknown color";
    }
    
    public int addProduct(Product product, List<String> colors, List<String> sizes) {
        String sql = "INSERT INTO products (name, name_vn, description, description_vn, brand, price, original_price, discount_percent, category_id, image_path, badge, gender, product_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                conn.setAutoCommit(false);
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, product.getName());
                    pstmt.setString(2, product.getNameVn());
                    pstmt.setString(3, product.getDescription());
                    pstmt.setString(4, product.getDescriptionVn());
                    pstmt.setString(5, product.getBrand());
                    pstmt.setBigDecimal(6, product.getPrice());
                    if (product.getOriginalPrice() != null) {
                        pstmt.setBigDecimal(7, product.getOriginalPrice());
                    } else {
                        pstmt.setNull(7, Types.DECIMAL);
                    }
                    pstmt.setInt(8, product.getDiscountPercent());
                    pstmt.setInt(9, product.getCategoryId());
                    pstmt.setString(10, product.getImagePath());
                    pstmt.setString(11, product.getBadge());
                    pstmt.setString(12, product.getGender());
                    pstmt.setString(13, product.getProductType());
                    
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        ResultSet rs = pstmt.getGeneratedKeys();
                        if (rs.next()) {
                            int productId = rs.getInt(1);
                            
                            // Insert colors
                            if (colors != null && !colors.isEmpty()) {
                                String colorSql = "INSERT INTO product_colors (product_id, color_code, color_name) VALUES (?, ?, ?)";
                                try (PreparedStatement colorStmt = conn.prepareStatement(colorSql)) {
                                    for (String colorCode : colors) {
                                        colorCode = colorCode.trim();
                                        if (!colorCode.isEmpty()) {
                                            colorStmt.setInt(1, productId);
                                            colorStmt.setString(2, colorCode);
                                            // Try to get color name from code
                                            String colorName = getColorNameFromCode(colorCode);
                                            colorStmt.setString(3, colorName);
                                            colorStmt.executeUpdate();
                                        }
                                    }
                                }
                            }
                            
                            // Insert sizes (đã sắp xếp theo thứ tự từ nhỏ đến lớn)
                            if (sizes != null && !sizes.isEmpty()) {
                                List<String> sortedSizes = sortSizes(new ArrayList<>(sizes));
                                String sizeSql = "INSERT INTO product_sizes (product_id, size) VALUES (?, ?)";
                                try (PreparedStatement sizeStmt = conn.prepareStatement(sizeSql)) {
                                    for (String size : sortedSizes) {
                                        size = size.trim();
                                        if (!size.isEmpty()) {
                                            sizeStmt.setInt(1, productId);
                                            sizeStmt.setString(2, size);
                                            sizeStmt.executeUpdate();
                                        }
                                    }
                                }
                            }
                            
                            conn.commit();
                            return productId;
                        }
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return -1;
    }
    
    private String getColorNameFromCode(String colorCode) {
        if (colorCode == null) return "Unknown";
        String codeLower = colorCode.toLowerCase();
        switch (codeLower) {
            case "#1e3a5f": return "Navy Blue";
            case "#ffffff": return "White";
            case "#e0e0e0": return "Light Gray";
            case "#8b4513": return "Brown";
            case "#000000": return "Black";
            case "#ff0000": return "Red";
            case "#0000ff": return "Blue";
            case "#00ff00": return "Green";
            default: return "Unknown";
        }
    }
}

