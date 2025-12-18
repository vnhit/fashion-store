package com.fashionstore.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.fashionstore.services.ProductService;
import com.fashionstore.services.CartService;
import com.fashionstore.services.FavoritesService;
import com.fashionstore.dao.UserDAO;
import com.fashionstore.models.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class HomeController {
    private ProductService productService = new ProductService();
    private CartService cartService = new CartService();
    private FavoritesService favoritesService = new FavoritesService();
    private int currentUserId = UserDAO.getCurrentUserId();
    // Dùng cùng một ký tự tim rỗng cho cả 2 trạng thái để tránh lỗi font ở ký tự tim đặc
    private static final String HEART_OUTLINE = "\u2661"; // ♡
    private static final String HEART_SOLID = "\u2661";   // ♡ (active sẽ phân biệt bằng màu nền)
    
    public void show(Stage stage) {
        VBox root = new VBox();
        root.getStyleClass().add("root");
        
        // Header with search functionality
        HBox header = createHeader(stage);
        
        // Hero Banner
        VBox heroBanner = createHeroBanner(stage);
        
        // Featured Products
        VBox featuredSection = createFeaturedProductsSection(stage);
        
        // Promotional Banner
        HBox promoBanner = createPromoBanner();
        
        // Recommended Products
        VBox recommendedSection = createRecommendedSection(stage);
        
        // Bottom Navigation
        HBox bottomNav = createBottomNavigation(stage);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox content = new VBox(25);
        content.setPadding(new javafx.geometry.Insets(0, 0, 20, 0));
        content.getChildren().addAll(heroBanner, featuredSection, promoBanner, recommendedSection);
        scrollPane.setContent(content);
        
        root.getChildren().addAll(header, scrollPane, bottomNav);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Giảm kích thước mặc định để phù hợp màn hình nhỏ hơn, vẫn cho phóng to
        Scene scene = new Scene(root, 1200, 800);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        stage.setScene(scene);
        stage.setTitle("FashionStore - Trang chủ");
        stage.setMinWidth(1024);
        stage.setMinHeight(640);
        stage.show();
    }
    
    private HBox createHeader(Stage stage) {
        HBox header = new HBox(20);
        header.getStyleClass().add("header");
        header.setPadding(new javafx.geometry.Insets(20, 30, 20, 30));
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Logo
        HBox logoBox = new HBox(10);
        Label logoIcon = new Label("👔");
        logoIcon.setStyle("-fx-font-size: 32px;");
        Label logoText = new Label("FashionStore");
        logoText.getStyleClass().add("logo");
        logoBox.getChildren().addAll(logoIcon, logoText);
        logoBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Search bar
        HBox searchBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Tìm kiếm sản phẩm, thương hiệu...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(500);
        
        Button searchBtn = new Button("Tìm kiếm");
        searchBtn.getStyleClass().add("search-button");
        searchBtn.setOnAction(e -> {
            String keyword = searchField.getText();
            ProductListController productListController = new ProductListController();
            productListController.showWithSearch(stage, keyword);
        });
        
        searchField.setOnAction(e -> searchBtn.fire());
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBox.getChildren().addAll(searchField, searchBtn);
        HBox.setHgrow(searchBox, Priority.ALWAYS);
        
        // Right side icons
        HBox iconBox = new HBox(15);
        iconBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button notificationBtn = new Button("🔔");
        notificationBtn.getStyleClass().add("icon-button");
        
        StackPane cartContainer = new StackPane();
        Button cartBtn = new Button("🛍️");
        cartBtn.getStyleClass().add("icon-button");
        cartBtn.setOnAction(e -> {
            CartController cartController = new CartController();
            cartController.show(stage);
        });
        
        // Cart badge
        int cartCount = cartService.getCartItemCount(currentUserId);
        if (cartCount > 0) {
            Label cartBadge = new Label(String.valueOf(cartCount));
            cartBadge.getStyleClass().add("cart-badge");
            StackPane.setAlignment(cartBadge, javafx.geometry.Pos.TOP_RIGHT);
            StackPane.setMargin(cartBadge, new javafx.geometry.Insets(-5, 40, 0, 0));
            cartContainer.getChildren().addAll(cartBtn, cartBadge);
        } else {
            cartContainer.getChildren().add(cartBtn);
        }
        
        // User icon button
        Button userBtn = new Button("👤");
        userBtn.getStyleClass().add("icon-button");
        userBtn.setOnAction(e -> {
            UserInfoDialog.show(stage);
        });
        
        iconBox.getChildren().addAll(notificationBtn, cartContainer, userBtn);
        
        header.getChildren().addAll(logoBox, searchBox, iconBox);
        return header;
    }
    
    private VBox createHeroBanner(Stage stage) {
        VBox banner = new VBox(15);
        banner.getStyleClass().add("hero-banner");
        banner.setPadding(new javafx.geometry.Insets(60, 50, 50, 50));
        banner.setPrefHeight(350);
        banner.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label badge = new Label("✨ MỚI NHẤT");
        badge.getStyleClass().add("banner-badge");
        
        Label title = new Label("Bộ Sưu Tập Hè 2024");
        title.getStyleClass().add("banner-title");
        
        Label subtitle = new Label("Giảm đến 50% cho các mẫu mới - Miễn phí vận chuyển cho đơn hàng trên 500.000₫");
        subtitle.getStyleClass().add("banner-subtitle");
        
        Button exploreBtn = new Button("Khám phá ngay →");
        exploreBtn.getStyleClass().add("banner-button");
        exploreBtn.setOnAction(e -> {
            ProductListController productListController = new ProductListController();
            productListController.show(stage);
        });
        
        VBox textContent = new VBox(15);
        textContent.getChildren().addAll(badge, title, subtitle, exploreBtn);
        textContent.setMaxWidth(600);
        
        banner.getChildren().add(textContent);
        return banner;
    }
    
    private VBox createFeaturedProductsSection(Stage stage) {
        VBox section = new VBox(20);
        section.setPadding(new javafx.geometry.Insets(30));
        
        HBox sectionHeader = new HBox();
        sectionHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(sectionHeader, Priority.ALWAYS);
        
        Label title = new Label("⭐ Sản phẩm nổi bật");
        title.getStyleClass().add("section-title-large");
        
        Hyperlink viewAll = new Hyperlink("Xem tất cả ");
        viewAll.getStyleClass().add("view-all-link-modern");
        viewAll.setOnAction(e -> {
            ProductListController productListController = new ProductListController();
            productListController.show(stage);
        });
        
        HBox.setHgrow(title, Priority.ALWAYS);
        sectionHeader.getChildren().addAll(title, viewAll);
        
        ScrollPane productsScroll = new ScrollPane();
        productsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        productsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        productsScroll.setPannable(true);
        productsScroll.setFitToHeight(true); // đảm bảo đủ chiều cao, tránh cuộn dọc trong khung
        productsScroll.setFitToWidth(false);
        productsScroll.setPadding(new javafx.geometry.Insets(0, 0, 20, 0)); // chừa đáy cho nút
        productsScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        productsScroll.setPrefHeight(450);
        productsScroll.setMinHeight(450);
        
        HBox productsRow = new HBox(20);
        // padding bottom lớn hơn để không bị thanh cuộn che phần nút
        productsRow.setPadding(new javafx.geometry.Insets(15, 20, 40, 20));
        productsRow.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        productsRow.setMinHeight(400);
        
        List<Product> featuredProducts = productService.getFeaturedProducts();
        Set<Integer> addedProductIds = new HashSet<>();
        if (featuredProducts != null && !featuredProducts.isEmpty()) {
            for (Product product : featuredProducts) {
                // Tránh trùng lặp products
                if (addedProductIds.contains(product.getId())) {
                    continue;
                }
                addedProductIds.add(product.getId());
                
                VBox productCard = createProductCard(product, stage);
                productsRow.getChildren().add(productCard);
            }
        } else {
            Label noProducts = new Label("Chưa có sản phẩm nổi bật");
            noProducts.getStyleClass().add("no-products-label");
            productsRow.getChildren().add(noProducts);
        }
        
        productsScroll.setContent(productsRow);
        section.getChildren().addAll(sectionHeader, productsScroll);
        
        return section;
    }
    
    private HBox createPromoBanner() {
        HBox banner = new HBox(30);
        banner.getStyleClass().add("promo-banner-modern");
        banner.setPadding(new javafx.geometry.Insets(40, 50, 40, 50));
        banner.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        VBox textContent = new VBox(15);
        Label title = new Label("🎁 Free Ship");
        title.getStyleClass().add("promo-title-modern");
        
        Label subtitle = new Label("Cho từ 2 đơn hàng của bạn");
        subtitle.getStyleClass().add("promo-subtitle-modern");
        
        Button getBtn = new Button("Nhận ngay →");
        getBtn.getStyleClass().add("promo-button-modern");
        
        textContent.getChildren().addAll(title, subtitle, getBtn);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        banner.getChildren().add(textContent);
        
        return banner;
    }
    
    private VBox createRecommendedSection(Stage stage) {
        VBox section = new VBox(20);
        section.setPadding(new javafx.geometry.Insets(30));
        
        HBox sectionHeader = new HBox();
        sectionHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(sectionHeader, Priority.ALWAYS);
        
        Label title = new Label("💝 Dành cho bạn");
        title.getStyleClass().add("section-title-large");
        
        Hyperlink viewMore = new Hyperlink("Xem thêm ");
        viewMore.getStyleClass().add("view-all-link-modern");
        viewMore.setOnAction(e -> {
            ProductListController productListController = new ProductListController();
            productListController.show(stage);
        });
        
        HBox.setHgrow(title, Priority.ALWAYS);
        sectionHeader.getChildren().addAll(title, viewMore);
        
        ScrollPane productsScroll = new ScrollPane();
        productsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        productsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        productsScroll.setPannable(true);
        productsScroll.setFitToHeight(true); // đảm bảo đủ chiều cao, tránh cuộn dọc trong khung
        productsScroll.setFitToWidth(false);
        productsScroll.setPadding(new javafx.geometry.Insets(0, 0, 20, 0)); // chừa đáy cho nút
        productsScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        productsScroll.setPrefHeight(450);
        productsScroll.setMinHeight(450);
        
        HBox productsRow = new HBox(20);
        // padding bottom lớn hơn để không bị thanh cuộn che phần nút
        productsRow.setPadding(new javafx.geometry.Insets(15, 20, 40, 20));
        productsRow.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        productsRow.setMinHeight(400);
        
        List<Product> allProducts = productService.getAllProducts();
        Set<Integer> addedProductIds = new HashSet<>();
        if (allProducts != null && !allProducts.isEmpty()) {
            for (Product product : allProducts) {
                // Tránh trùng lặp products
                if (addedProductIds.contains(product.getId())) {
                    continue;
                }
                addedProductIds.add(product.getId());
                
                VBox productCard = createProductCard(product, stage);
                productsRow.getChildren().add(productCard);
            }
        } else {
            Label noProducts = new Label("Chưa có sản phẩm");
            noProducts.getStyleClass().add("no-products-label");
            productsRow.getChildren().add(noProducts);
        }
        
        productsScroll.setContent(productsRow);
        section.getChildren().addAll(sectionHeader, productsScroll);
        
        return section;
    }
    
    private VBox createProductCard(Product product, Stage stage) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card-modern");
        card.setPrefWidth(280);
        card.setMinWidth(280);
        card.setMaxWidth(280);
        
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("product-image-container-modern");
        imageContainer.setPrefHeight(280);
        imageContainer.setPrefWidth(280);
        imageContainer.setMinHeight(280);
        imageContainer.setMinWidth(280);
        imageContainer.setMaxHeight(280);
        imageContainer.setMaxWidth(280);
        imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
        // Thêm padding để có khoảng trắng giữa nền xanh và viền thẻ
        imageContainer.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));
        
        Rectangle placeholder = new Rectangle(240, 240);
        placeholder.getStyleClass().add("product-image-placeholder-modern");
        imageContainer.getChildren().add(placeholder);

        // Load product image if available
        String imagePath = product.getImagePath();
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                String resourcePath = buildImageResourcePath(imagePath.trim());
                java.net.URL imgUrl = getClass().getResource(resourcePath);
                if (imgUrl != null) {
                    ImageView imageView = new ImageView(new Image(imgUrl.toExternalForm()));
                    // Ảnh nhỏ hơn container để có khoảng trắng xung quanh
                    imageView.setFitWidth(240);
                    imageView.setFitHeight(240);
                    imageView.setPreserveRatio(false);
                    imageView.setSmooth(true);
                    imageView.setCache(true);
                    StackPane.setAlignment(imageView, javafx.geometry.Pos.CENTER);
                    imageContainer.getChildren().add(imageView);
                }
            } catch (Exception ex) {
                System.err.println("Error loading product image: " + ex.getMessage());
            }
        }
        
        // Badge
        if (product.getBadge() != null && !product.getBadge().isEmpty()) {
            Label badge = new Label(product.getBadge());
            badge.getStyleClass().add("product-badge-modern");
            StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_LEFT);
            StackPane.setMargin(badge, new javafx.geometry.Insets(10, 10, 0, 0));
            imageContainer.getChildren().add(badge);
        }
        
        // Heart icon (yêu thích) với trạng thái theo DB
        Button heartBtn = new Button();
        heartBtn.getStyleClass().add("heart-button-modern");

        final boolean[] isFavorite = {favoritesService.isFavorite(currentUserId, product.getId())};
        updateHeartButtonState(heartBtn, isFavorite[0]);

        heartBtn.setOnAction(e -> {
            if (isFavorite[0]) {
                favoritesService.removeFavorite(currentUserId, product.getId());
                isFavorite[0] = false;
            } else {
                favoritesService.addFavorite(currentUserId, product.getId());
                isFavorite[0] = true;
            }
            updateHeartButtonState(heartBtn, isFavorite[0]);
            e.consume();
        });
        StackPane.setAlignment(heartBtn, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(heartBtn, new javafx.geometry.Insets(10, 10, 0, 0));
        imageContainer.getChildren().add(heartBtn);
        
        // Discount badge
        if (product.getDiscountPercent() > 0) {
            Label discount = new Label("-" + product.getDiscountPercent() + "%");
            discount.getStyleClass().add("discount-badge-modern");
            StackPane.setAlignment(discount, javafx.geometry.Pos.BOTTOM_LEFT);
            StackPane.setMargin(discount, new javafx.geometry.Insets(0, 0, 10, 10));
            imageContainer.getChildren().add(discount);
        }
        
        VBox infoBox = new VBox(8);
        infoBox.setPadding(new javafx.geometry.Insets(0, 15, 15, 15));
        infoBox.setPrefWidth(280);
        
        Label brandLabel = new Label(product.getBrand());
        brandLabel.getStyleClass().add("product-brand-modern");
        brandLabel.setAlignment(javafx.geometry.Pos.CENTER);
        brandLabel.setMaxWidth(Double.MAX_VALUE);
        
        Label nameLabel = new Label(product.getNameVn());
        nameLabel.getStyleClass().add("product-name-modern");
        nameLabel.setWrapText(true);
        nameLabel.setPrefHeight(40);
        nameLabel.setMinHeight(40);
        nameLabel.setAlignment(javafx.geometry.Pos.CENTER);
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        HBox priceRow = new HBox(10);
        priceRow.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label priceLabel = new Label(formatPrice(product.getPrice()));
        priceLabel.getStyleClass().add("product-price-modern");
        
        if (product.getOriginalPrice() != null && product.getOriginalPrice().compareTo(product.getPrice()) > 0) {
            Text originalPrice = new Text(formatPrice(product.getOriginalPrice()));
            originalPrice.getStyleClass().add("original-price-modern");
            originalPrice.setStrikethrough(true);
            originalPrice.setStyle("-fx-font-size: 14px; -fx-fill: #999;");
            priceRow.getChildren().addAll(priceLabel, originalPrice);
        } else {
            priceRow.getChildren().add(priceLabel);
        }
        
        infoBox.getChildren().addAll(brandLabel, nameLabel, priceRow);
        
        card.getChildren().addAll(imageContainer, infoBox);
        
        card.setOnMouseClicked(e -> {
            ProductDetailController detailController = new ProductDetailController();
            detailController.show(product, stage);
        });
        
        return card;
    }

    private void updateHeartButtonState(Button heartBtn, boolean isFavorite) {
        heartBtn.setText(""); // dùng graphic để tránh lỗi hiển thị
        heartBtn.setGraphic(buildHeartGraphic(isFavorite ? HEART_SOLID : HEART_OUTLINE, isFavorite));
        heartBtn.getStyleClass().remove("heart-button-active");
        if (isFavorite) {
            heartBtn.getStyleClass().add("heart-button-active");
        }
    }

    /**
     * Chuẩn hóa đường dẫn ảnh: chấp nhận "file.jpg", "images/file.jpg" hoặc "/images/file.jpg"
     * và luôn trả về dạng "/images/file.jpg" để load từ resources.
     */
    private String buildImageResourcePath(String imagePath) {
        String path = imagePath;
        if (path.startsWith("/")) {
            return path;
        }
        if (path.startsWith("images/")) {
            return "/" + path;
        }
        return "/images/" + path;
    }

    private Text buildHeartGraphic(String symbol, boolean active) {
        Text icon = new Text(symbol);
        icon.setStyle("-fx-font-size: 20px; -fx-font-family: 'Segoe UI Symbol', 'Segoe UI Emoji', 'Arial Unicode MS', 'Arial';"
                + (active ? " -fx-fill: white;" : " -fx-fill: #333;"));
        return icon;
    }
    
    private HBox createBottomNavigation(Stage stage) {
        HBox nav = new HBox();
        nav.getStyleClass().add("bottom-nav-modern");
        nav.setPadding(new javafx.geometry.Insets(15));
        
        String[] navItems = {"Trang chủ", "Danh mục", "Yêu thích", "Tài khoản"};
        String[] navIcons = {"🏠", "📦", "❤️", "👤"};
        
        for (int i = 0; i < navItems.length; i++) {
            VBox navItem = new VBox(5);
            navItem.setAlignment(javafx.geometry.Pos.CENTER);
            navItem.getStyleClass().add("nav-item-modern");
            if (i == 0) {
                navItem.getStyleClass().add("nav-item-active-modern");
            }
            
            Label icon = new Label(navIcons[i]);
            icon.getStyleClass().add("nav-icon-modern");
            
            Label label = new Label(navItems[i]);
            label.getStyleClass().add("nav-label-modern");
            
            navItem.getChildren().addAll(icon, label);
            
            final int index = i;
            navItem.setOnMouseClicked(e -> {
                if (index == 0) {
                    HomeController homeController = new HomeController();
                    homeController.show(stage);
                } else if (index == 1) {
                    ProductListController productListController = new ProductListController();
                    productListController.show(stage);
                } else if (index == 2) {
                    FavoritesController favoritesController = new FavoritesController();
                    favoritesController.show(stage);
                } else if (index == 3) {
                    AccountController accountController = new AccountController();
                    accountController.show(stage);
                }
            });
            
            HBox.setHgrow(navItem, Priority.ALWAYS);
            nav.getChildren().add(navItem);
        }
        
        return nav;
    }
    
    private String formatPrice(BigDecimal price) {
        if (price == null) return "0₫";
        return String.format("%,d₫", price.intValue());
    }
}
