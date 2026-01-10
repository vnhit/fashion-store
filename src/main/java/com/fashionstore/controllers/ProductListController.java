package com.fashionstore.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.fashionstore.services.ProductService;
import com.fashionstore.services.CartService;
import com.fashionstore.services.FavoritesService;
import com.fashionstore.dao.CategoryDAO;
import com.fashionstore.dao.UserDAO;
import com.fashionstore.dao.NotificationDAO;
import com.fashionstore.models.Product;
import com.fashionstore.models.Category;
import com.fashionstore.models.Notification;
import java.time.format.DateTimeFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.math.BigDecimal;
import java.util.List;

public class ProductListController {
    private ProductService productService = new ProductService();
    private CartService cartService = new CartService();
    private FavoritesService favoritesService = new FavoritesService();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private NotificationDAO notificationDAO = new NotificationDAO();
    private int currentUserId = UserDAO.getCurrentUserId();
    // Dùng cùng một ký tự tim rỗng cho cả 2 trạng thái để tránh lỗi font ở ký tự tim đặc
    private static final String HEART_OUTLINE = "\u2661"; // ♡
    private static final String HEART_SOLID = "\u2661";   // ♡ (active sẽ phân biệt bằng màu nền)
    
    private Integer selectedCategoryId = null;
    private String selectedBrand = null;
    private BigDecimal minPrice = null;
    private BigDecimal maxPrice = null;
    private String sortBy = "newest";
    private String searchKeyword = "";
    
    public void show(Stage stage) {
        showWithSearch(stage, "");
    }
    
    public void showWithSearch(Stage stage, String keyword) {
        this.searchKeyword = keyword;
        buildUI(stage);
    }
    
    public void showWithCategory(Stage stage, int categoryId) {
        this.selectedCategoryId = categoryId;
        buildUI(stage);
    }
    
    private void buildUI(Stage stage) {
        VBox root = new VBox();
        root.getStyleClass().add("root");
        
        // Header
        HBox header = createHeader(stage);
        
        // Filter and Search Bar
        VBox filterSection = createFilterSection(stage);
        
        // Product grid
        ScrollPane scrollPane = new ScrollPane();
        GridPane productGrid = createProductGrid();
        scrollPane.setContent(productGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent;");
        
        // Bottom Navigation
        HBox bottomNav = createBottomNavigation(stage);
        
        root.getChildren().addAll(header, filterSection, scrollPane, bottomNav);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Giảm kích thước mặc định để phù hợp màn hình nhỏ hơn, vẫn cho phóng to
        Scene scene = new Scene(root, 1200, 800);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        stage.setScene(scene);
        stage.setTitle("FashionStore - Sản phẩm");
        stage.setMinWidth(1024);
        stage.setMinHeight(640);
        stage.show();
    }
    
    private HBox createHeader(Stage stage) {
        HBox header = new HBox(20);
        header.getStyleClass().add("header");
        header.setPadding(new javafx.geometry.Insets(18, 40, 18, 40));
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Button backBtn = new Button();
        backBtn.getStyleClass().add("back-button");
        backBtn.setText("");
        Text backIcon = new Text("←");
        backIcon.setStyle("-fx-font-size: 24px; -fx-font-family: 'Segoe UI Symbol', 'Segoe UI Emoji', 'Arial Unicode MS', 'Arial';");
        backBtn.setGraphic(backIcon);
        backBtn.setPrefWidth(40);
        backBtn.setPrefHeight(40);
        backBtn.setOnAction(e -> {
            HomeController homeController = new HomeController();
            homeController.show(stage);
        });
        
        Label title = new Label("Sản phẩm");
        title.getStyleClass().add("page-title");
        title.setPadding(new javafx.geometry.Insets(0, 0, 0, 10));
        
        HBox searchBox = new HBox(12);
        searchBox.setAlignment(javafx.geometry.Pos.CENTER);
        TextField searchField = new TextField();
        if (!searchKeyword.isEmpty()) {
            searchField.setText(searchKeyword);
        }
        searchField.setPromptText("🔍 Tìm kiếm...");
        searchField.getStyleClass().add("search-field-small");
        searchField.setPrefWidth(450);
        searchField.setPrefHeight(38);
        
        Button searchBtn = new Button("Tìm");
        searchBtn.getStyleClass().add("search-button-small");
        searchBtn.setPrefHeight(38);
        searchBtn.setOnAction(e -> {
            searchKeyword = searchField.getText();
            buildUI(stage);
        });
        
        searchField.setOnAction(e -> searchBtn.fire());
        searchBox.getChildren().addAll(searchField, searchBtn);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox iconBox = new HBox(15);
        iconBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        StackPane cartContainer = new StackPane();
        cartContainer.setAlignment(javafx.geometry.Pos.CENTER);
        Button cartBtn = new Button();
        cartBtn.getStyleClass().add("icon-button");
        cartBtn.setText("");
        Text cartIcon = new Text("🛍️");
        cartIcon.setStyle("-fx-font-size: 22px; -fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Arial Unicode MS', 'Arial';");
        cartBtn.setGraphic(cartIcon);
        cartBtn.setPrefWidth(40);
        cartBtn.setPrefHeight(40);
        cartBtn.setOnAction(e -> {
            CartController cartController = new CartController();
            cartController.show(stage);
        });
        
        int cartCount = cartService.getCartItemCount(currentUserId);
        if (cartCount > 0) {
            Label cartBadge = new Label(String.valueOf(cartCount));
            cartBadge.getStyleClass().add("cart-badge");
            StackPane.setAlignment(cartBadge, javafx.geometry.Pos.TOP_RIGHT);
            StackPane.setMargin(cartBadge, new javafx.geometry.Insets(1, 28, 0, 0));
            cartContainer.getChildren().addAll(cartBtn, cartBadge);
        } else {
            cartContainer.getChildren().add(cartBtn);
        }
        
        // Notification button
        StackPane notificationContainer = new StackPane();
        notificationContainer.setPrefWidth(40);
        notificationContainer.setPrefHeight(40);
        notificationContainer.setMaxWidth(40);
        notificationContainer.setMaxHeight(40);
        Button notificationBtn = new Button();
        notificationBtn.getStyleClass().add("icon-button");
        notificationBtn.setText("");
        Text notificationIcon = new Text("🔔");
        notificationIcon.setStyle("-fx-font-size: 22px; -fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Arial Unicode MS', 'Arial';");
        notificationBtn.setGraphic(notificationIcon);
        notificationBtn.setPrefWidth(40);
        notificationBtn.setPrefHeight(40);
        
        // Hiển thị badge số thông báo chưa đọc
        int unreadCount = notificationDAO.getUnreadCount(currentUserId);
        if (unreadCount > 0) {
            Label notificationBadge = new Label(String.valueOf(unreadCount));
            notificationBadge.getStyleClass().add("cart-badge");
            StackPane.setAlignment(notificationBadge, javafx.geometry.Pos.TOP_RIGHT);
            StackPane.setMargin(notificationBadge, new javafx.geometry.Insets(1, 10, 0, 0));
            notificationContainer.getChildren().addAll(notificationBtn, notificationBadge);
        } else {
            notificationContainer.getChildren().add(notificationBtn);
        }
        
        notificationBtn.setOnAction(e -> {
            showNotificationsDialog(stage);
        });
        
        // User icon button
        Button userBtn = new Button();
        userBtn.getStyleClass().add("icon-button");
        userBtn.setText("");
        Text userIcon = new Text("👤");
        userIcon.setStyle("-fx-font-size: 22px; -fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Arial Unicode MS', 'Arial';");
        userBtn.setGraphic(userIcon);
        userBtn.setPrefWidth(40);
        userBtn.setPrefHeight(40);
        userBtn.setOnAction(e -> {
            UserInfoDialog.show(stage);
        });
        
        iconBox.getChildren().addAll(notificationContainer, cartContainer, userBtn);
        header.getChildren().addAll(backBtn, title, searchBox, spacer, iconBox);
        return header;
    }
    
    private VBox createFilterSection(Stage stage) {
        VBox filterSection = new VBox(12);
        filterSection.setPadding(new javafx.geometry.Insets(18, 40, 18, 40));
        filterSection.getStyleClass().add("filter-section");
        
        // Filter buttons row
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Category filter
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().add("Tất cả danh mục");
        List<Category> categories = categoryDAO.getAllCategories();
        for (Category cat : categories) {
            categoryCombo.getItems().add(cat.getNameVn());
        }
        String currentCategoryText = "Tất cả danh mục";
        if (selectedCategoryId != null) {
            for (Category cat : categories) {
                if (cat.getId() == selectedCategoryId) {
                    currentCategoryText = cat.getNameVn();
                    break;
                }
            }
        }
        categoryCombo.setValue(currentCategoryText);
        categoryCombo.setPrefWidth(220);
        categoryCombo.setPrefHeight(38);
        categoryCombo.getStyleClass().add("filter-combo");
        categoryCombo.setOnAction(e -> {
            String selected = categoryCombo.getValue();
            if (selected.equals("Tất cả danh mục")) {
                selectedCategoryId = null;
            } else {
                for (Category cat : categories) {
                    if (cat.getNameVn().equals(selected)) {
                        selectedCategoryId = cat.getId();
                        break;
                    }
                }
            }
            buildUI(stage);
        });
        
        // Brand filter
        ComboBox<String> brandCombo = new ComboBox<>();
        brandCombo.getItems().add("Tất cả thương hiệu");
        List<String> brands = productService.getAllBrands();
        brandCombo.getItems().addAll(brands);
        String currentBrandText = selectedBrand != null ? selectedBrand : "Tất cả thương hiệu";
        brandCombo.setValue(currentBrandText);
        brandCombo.setPrefWidth(220);
        brandCombo.setPrefHeight(38);
        brandCombo.getStyleClass().add("filter-combo");
        brandCombo.setOnAction(e -> {
            String selected = brandCombo.getValue();
            selectedBrand = selected.equals("Tất cả thương hiệu") ? null : selected;
            buildUI(stage);
        });
        
        // Price range section
        HBox priceBox = new HBox(8);
        priceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label priceLabel = new Label("Giá:");
        priceLabel.getStyleClass().add("price-label");
        
        TextField minPriceField = new TextField();
        minPriceField.setPromptText("Tối thiểu");
        minPriceField.setPrefWidth(130);
        minPriceField.setPrefHeight(38);
        minPriceField.getStyleClass().add("price-input");
        if (minPrice != null) {
            minPriceField.setText(minPrice.toString());
        }
        
        Label dashLabel = new Label("-");
        dashLabel.getStyleClass().add("price-dash");
        
        TextField maxPriceField = new TextField();
        maxPriceField.setPromptText("Tối đa");
        maxPriceField.setPrefWidth(130);
        maxPriceField.setPrefHeight(38);
        maxPriceField.getStyleClass().add("price-input");
        if (maxPrice != null) {
            maxPriceField.setText(maxPrice.toString());
        }
        
        Button applyPriceBtn = new Button("Áp dụng");
        applyPriceBtn.getStyleClass().add("apply-filter-button");
        applyPriceBtn.setPrefHeight(38);
        applyPriceBtn.setOnAction(e -> {
            try {
                minPrice = minPriceField.getText().isEmpty() ? null : new BigDecimal(minPriceField.getText());
                maxPrice = maxPriceField.getText().isEmpty() ? null : new BigDecimal(maxPriceField.getText());
                buildUI(stage);
            } catch (NumberFormatException ex) {
                // Invalid price
            }
        });
        
        priceBox.getChildren().addAll(priceLabel, minPriceField, dashLabel, maxPriceField, applyPriceBtn);

        // Sort section (price & rating asc/desc)
        HBox sortBox = new HBox(8);
        sortBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label sortLabel = new Label("Sắp xếp:");
        sortLabel.getStyleClass().add("sort-label");

        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll(
            "Mới nhất",
            "Giá: thấp đến cao",
            "Giá: cao đến thấp",
            "Sao: thấp đến cao",
            "Sao: cao đến thấp"
        );

        String currentSortText;
        switch (sortBy) {
            case "price_asc":
                currentSortText = "Giá: thấp đến cao";
                break;
            case "price_desc":
                currentSortText = "Giá: cao đến thấp";
                break;
            case "rating_asc":
                currentSortText = "Sao: thấp đến cao";
                break;
            case "rating_desc":
                currentSortText = "Sao: cao đến thấp";
                break;
            default:
                currentSortText = "Mới nhất";
        }

        sortCombo.setValue(currentSortText);
        sortCombo.setPrefWidth(180);
        sortCombo.setPrefHeight(38);
        sortCombo.getStyleClass().add("filter-combo");

        sortCombo.setOnAction(e -> {
            String selected = sortCombo.getValue();
            if ("Giá: thấp đến cao".equals(selected)) {
                sortBy = "price_asc";
            } else if ("Giá: cao đến thấp".equals(selected)) {
                sortBy = "price_desc";
            } else if ("Sao: thấp đến cao".equals(selected)) {
                sortBy = "rating_asc";
            } else if ("Sao: cao đến thấp".equals(selected)) {
                sortBy = "rating_desc";
            } else {
                sortBy = "newest";
            }
            buildUI(stage);
        });

        sortBox.getChildren().addAll(sortLabel, sortCombo);
        
        filterRow.getChildren().addAll(categoryCombo, brandCombo, priceBox, sortBox);
        
        // Active filters display
        HBox activeFilters = new HBox(10);
        activeFilters.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        activeFilters.setPadding(new javafx.geometry.Insets(5, 0, 0, 0));
        
        if (selectedCategoryId != null) {
            for (Category cat : categories) {
                if (cat.getId() == selectedCategoryId) {
                    Button activeFilter = new Button(cat.getNameVn() + " ×");
                    activeFilter.getStyleClass().add("active-filter-tag");
                    activeFilter.setOnAction(e -> {
                        selectedCategoryId = null;
                        buildUI(stage);
                    });
                    activeFilters.getChildren().add(activeFilter);
                    break;
                }
            }
        }
        
        if (selectedBrand != null) {
            Button brandFilter = new Button(selectedBrand + " ×");
            brandFilter.getStyleClass().add("active-filter-tag");
            brandFilter.setOnAction(e -> {
                selectedBrand = null;
                buildUI(stage);
            });
            activeFilters.getChildren().add(brandFilter);
        }
        
        if (minPrice != null || maxPrice != null) {
            String priceText = "";
            if (minPrice != null && maxPrice != null) {
                priceText = formatPrice(minPrice) + " - " + formatPrice(maxPrice);
            } else if (minPrice != null) {
                priceText = "Từ " + formatPrice(minPrice);
            } else {
                priceText = "Đến " + formatPrice(maxPrice);
            }
            Button priceFilter = new Button(priceText + " ×");
            priceFilter.getStyleClass().add("active-filter-tag");
            priceFilter.setOnAction(e -> {
                minPrice = null;
                maxPrice = null;
                buildUI(stage);
            });
            activeFilters.getChildren().add(priceFilter);
        }
        
        Button clearAllBtn = new Button("Xóa tất cả");
        clearAllBtn.getStyleClass().add("clear-filter-button");
        clearAllBtn.setOnAction(e -> {
            selectedCategoryId = null;
            selectedBrand = null;
            minPrice = null;
            maxPrice = null;
            sortBy = "newest";
            searchKeyword = "";
            buildUI(stage);
        });
        
        if (!activeFilters.getChildren().isEmpty()) {
            activeFilters.getChildren().add(clearAllBtn);
        }
        
        filterSection.getChildren().addAll(filterRow, activeFilters);
        return filterSection;
    }
    
    private GridPane createProductGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new javafx.geometry.Insets(25, 30, 40, 30));
        grid.setHgap(20);
        grid.setVgap(25);
        grid.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        
        List<Product> products;
        if (!searchKeyword.isEmpty()) {
            products = productService.searchProducts(searchKeyword);
        } else {
            products = productService.filterProducts(selectedCategoryId, selectedBrand, minPrice, maxPrice, sortBy);
        }
        
        int col = 0;
        int row = 0;
        int colsPerRow = 4;
        
        if (products.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(javafx.geometry.Pos.CENTER);
            emptyBox.setPrefHeight(400);
            Label noProducts = new Label("Không tìm thấy sản phẩm nào");
            noProducts.getStyleClass().add("no-products-label");
            emptyBox.getChildren().add(noProducts);
            grid.add(emptyBox, 0, 0, colsPerRow, 1);
        } else {
            for (Product product : products) {
                VBox productCard = createProductCard(product);
                grid.add(productCard, col, row);
                
                col++;
                if (col >= colsPerRow) {
                    col = 0;
                    row++;
                }
            }
        }
        
        return grid;
    }
    
    private VBox createProductCard(Product product) {
        VBox card = new VBox(0);
        card.getStyleClass().add("product-card-modern");
        card.setPrefWidth(280);
        card.setMinWidth(280);
        card.setMaxWidth(280);
        // Căn giữa nội dung để khoảng trắng hai bên đều
        card.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("product-image-container-modern");
        imageContainer.setPrefHeight(280);
        imageContainer.setPrefWidth(240);
        imageContainer.setMinHeight(280);
        imageContainer.setMinWidth(240);
        imageContainer.setMaxHeight(280);
        imageContainer.setMaxWidth(240);
        imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
        imageContainer.setClip(new javafx.scene.shape.Rectangle(240, 280));
        
        javafx.scene.shape.Rectangle placeholder = new javafx.scene.shape.Rectangle(240, 280);
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
                    imageView.setFitWidth(240);
                    imageView.setFitHeight(280);
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
        
        if (product.getBadge() != null && !product.getBadge().isEmpty()) {
            Label badge = new Label(product.getBadge());
            badge.getStyleClass().add("product-badge-modern");
            StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_LEFT);
            StackPane.setMargin(badge, new javafx.geometry.Insets(12, 12, 0, 0));
            imageContainer.getChildren().add(badge);
        }
        
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
        
        if (product.getDiscountPercent() > 0) {
            Label discount = new Label("-" + product.getDiscountPercent() + "%");
            discount.getStyleClass().add("discount-badge-modern");
            StackPane.setAlignment(discount, javafx.geometry.Pos.BOTTOM_LEFT);
            StackPane.setMargin(discount, new javafx.geometry.Insets(0, 0, 12, 12));
            imageContainer.getChildren().add(discount);
        }
        
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new javafx.geometry.Insets(15, 15, 18, 15));
        infoBox.setPrefWidth(280);
        
        Label nameLabel = new Label(product.getNameVn());
        nameLabel.getStyleClass().add("product-name-modern");
        nameLabel.setWrapText(true);
        nameLabel.setPrefHeight(45);
        nameLabel.setMinHeight(45);
        
        HBox ratingRow = new HBox(5);
        ratingRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        if (product.getRating() > 0) {
            Label rating = new Label("⭐ " + product.getRating());
            rating.getStyleClass().add("product-rating-modern");
            ratingRow.getChildren().add(rating);
        }
        
        HBox priceRow = new HBox(10);
        priceRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
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
        
        card.getChildren().addAll(imageContainer, infoBox);
        infoBox.getChildren().addAll(nameLabel, ratingRow, priceRow);
        
        card.setOnMouseClicked(e -> {
            ProductDetailController detailController = new ProductDetailController();
            detailController.show(product, (Stage) card.getScene().getWindow());
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
        
        String[] navItems = {"Trang chủ", "Cửa hàng", "Yêu thích", "Tài khoản"};
        String[] navIcons = {"🏠", "📦", "❤️", "👤"};
        
        for (int i = 0; i < navItems.length; i++) {
            VBox navItem = new VBox(5);
            navItem.setAlignment(javafx.geometry.Pos.CENTER);
            navItem.getStyleClass().add("nav-item-modern");
            if (i == 1) {
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
                    // Cửa hàng: đang ở trang này
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

    /**
     * Chuẩn hóa đường dẫn ảnh: "file.jpg", "images/file.jpg" hoặc "/images/file.jpg"
     * -> luôn về dạng "/images/file.jpg" để load từ resources.
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
    
    /**
     * Hiển thị dialog thông báo cho user
     */
    private void showNotificationsDialog(Stage owner) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initOwner(owner);
        dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
        dialog.setTitle("Thông báo");
        
        VBox root = new VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setPrefWidth(500);
        root.setPrefHeight(600);
        
        HBox header = new HBox();
        Label title = new Label("🔔 Thông báo");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        HBox.setHgrow(title, Priority.ALWAYS);
        header.getChildren().add(title);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox notificationsList = new VBox(10);
        notificationsList.setPadding(new javafx.geometry.Insets(10));
        
        List<Notification> notifications = notificationDAO.getNotificationsByUser(currentUserId);
        
        if (notifications.isEmpty()) {
            Label noNotifications = new Label("Không có thông báo nào");
            noNotifications.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            noNotifications.setAlignment(javafx.geometry.Pos.CENTER);
            notificationsList.getChildren().add(noNotifications);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Notification notification : notifications) {
                VBox notificationItem = new VBox(5);
                notificationItem.setPadding(new javafx.geometry.Insets(10));
                notificationItem.setStyle("-fx-background-color: " + 
                    (notification.isRead() ? "#f5f5f5" : "#e3f2fd") + "; " +
                    "-fx-background-radius: 8; " +
                    "-fx-border-color: #ddd; " +
                    "-fx-border-radius: 8;");
                
                HBox notificationHeader = new HBox(10);
                Text icon = new Text();
                if ("ORDER_SHIPPED".equals(notification.getType())) {
                    icon.setText("🚚");
                } else if ("ORDER_DELIVERED".equals(notification.getType())) {
                    icon.setText("✅");
                } else {
                    icon.setText("📦");
                }
                icon.setStyle("-fx-font-size: 20px; -fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Arial Unicode MS', 'Arial';");
                
                VBox notificationContent = new VBox(3);
                Label messageLabel = new Label(notification.getMessage());
                messageLabel.setWrapText(true);
                messageLabel.setStyle("-fx-font-size: 14px; " +
                    (notification.isRead() ? "-fx-text-fill: #666;" : "-fx-font-weight: bold;"));
                
                Label timeLabel = new Label(notification.getCreatedAt() != null ? 
                    notification.getCreatedAt().format(formatter) : "");
                timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
                
                notificationContent.getChildren().addAll(messageLabel, timeLabel);
                HBox.setHgrow(notificationContent, Priority.ALWAYS);
                
                notificationHeader.getChildren().addAll(icon, notificationContent);
                
                notificationItem.getChildren().add(notificationHeader);
                notificationsList.getChildren().add(notificationItem);
                
                // Đánh dấu đã đọc khi click vào thông báo
                notificationItem.setOnMouseClicked(e -> {
                    if (!notification.isRead()) {
                        notificationDAO.markAsRead(notification.getId());
                        notification.setRead(true);
                        // Refresh dialog
                        showNotificationsDialog(owner);
                    }
                });
            }
        }
        
        scrollPane.setContent(notificationsList);
        
        Button closeBtn = new Button("Đóng");
        closeBtn.getStyleClass().add("admin-btn");
        closeBtn.setPrefWidth(Double.MAX_VALUE);
        closeBtn.setOnAction(e -> dialog.close());
        
        root.getChildren().addAll(header, scrollPane, closeBtn);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            // Ignore
        }
        dialog.setScene(scene);
        dialog.show();
    }
}
