package com.fashionstore.controllers;

import com.fashionstore.dao.UserDAO;
import com.fashionstore.models.Product;
import com.fashionstore.services.FavoritesService;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FavoritesController {
    private final FavoritesService favoritesService = new FavoritesService();
    private final int currentUserId = UserDAO.getCurrentUserId();
    // Dùng tim rỗng giống trong header để tránh lỗi font tim đặc
    private static final String HEART_SOLID = "\u2661";   // ♡

    public void show(Stage stage) {
        VBox root = new VBox();
        root.getStyleClass().add("root");

        HBox header = createHeader(stage);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent;");

        VBox content = new VBox(20);
        content.setPadding(new javafx.geometry.Insets(30, 40, 40, 40));
        Label title = new Label("Sản phẩm yêu thích");
        title.getStyleClass().add("section-title-large");
        content.getChildren().add(title);

        List<Product> favoriteProducts = favoritesService.getFavorites(currentUserId);

        if (favoriteProducts.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(javafx.geometry.Pos.CENTER);
            empty.setPadding(new javafx.geometry.Insets(60, 0, 60, 0));
            Label emoji = new Label("🤍");
            emoji.setStyle("-fx-font-size: 42px;");
            Label msg = new Label("Chưa có sản phẩm yêu thích");
            msg.getStyleClass().add("no-products-label");
            empty.getChildren().addAll(emoji, msg);
            content.getChildren().add(empty);
        } else {
            GridPane grid = new GridPane();
            grid.setHgap(25);
            grid.setVgap(25);
            grid.setAlignment(javafx.geometry.Pos.TOP_CENTER);

            int col = 0;
            int row = 0;
            int colsPerRow = 3;
            for (Product product : favoriteProducts) {
                VBox productCard = createFavoriteCard(product, stage);
                grid.add(productCard, col, row);
                col++;
                if (col >= colsPerRow) {
                    col = 0;
                    row++;
                }
            }
            content.getChildren().add(grid);
        }

        scrollPane.setContent(content);

        HBox bottomNav = createBottomNavigation(stage);

        root.getChildren().addAll(header, scrollPane, bottomNav);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        stage.setScene(scene);
        stage.setTitle("FashionStore - Yêu thích");
        stage.setMinWidth(1024);
        stage.setMinHeight(640);
        stage.show();
    }

    private VBox createFavoriteCard(Product product, Stage stage) {
        VBox card = new VBox(0);
        card.getStyleClass().add("product-card-modern");
        card.setPrefWidth(280);
        card.setMinWidth(280);
        card.setMaxWidth(280);
        // Căn giữa nội dung để khoảng trắng hai bên đều
        card.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        javafx.scene.layout.StackPane imageContainer = new javafx.scene.layout.StackPane();
        imageContainer.getStyleClass().add("product-image-container-modern");
        imageContainer.setPrefHeight(270);
        imageContainer.setPrefWidth(240);
        imageContainer.setMinHeight(270);
        imageContainer.setMinWidth(240);
        imageContainer.setMaxHeight(270);
        imageContainer.setMaxWidth(240);
        imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
        imageContainer.setClip(new javafx.scene.shape.Rectangle(240, 270));

        javafx.scene.shape.Rectangle placeholder = new javafx.scene.shape.Rectangle(240, 270);
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
                    imageView.setFitHeight(270);
                    imageView.setPreserveRatio(false);
                    imageView.setSmooth(true);
                    imageView.setCache(true);
                    javafx.scene.layout.StackPane.setAlignment(imageView, javafx.geometry.Pos.CENTER);
                    imageContainer.getChildren().add(imageView);
                }
            } catch (Exception ex) {
                System.err.println("Error loading favorite product image: " + ex.getMessage());
            }
        }

        if (product.getBadge() != null && !product.getBadge().isEmpty()) {
            Label badge = new Label(product.getBadge());
            badge.getStyleClass().add("product-badge-modern");
            javafx.scene.layout.StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_LEFT);
            javafx.scene.layout.StackPane.setMargin(badge, new javafx.geometry.Insets(12, 12, 0, 0));
            imageContainer.getChildren().add(badge);
        }

        if (product.getDiscountPercent() > 0) {
            Label discount = new Label("-" + product.getDiscountPercent() + "%");
            discount.getStyleClass().add("discount-badge-modern");
            javafx.scene.layout.StackPane.setAlignment(discount, javafx.geometry.Pos.BOTTOM_LEFT);
            javafx.scene.layout.StackPane.setMargin(discount, new javafx.geometry.Insets(0, 0, 12, 12));
            imageContainer.getChildren().add(discount);
        }

        // Nút tim để hủy yêu thích ngay trong danh sách yêu thích
        Button heartBtn = new Button();
        heartBtn.getStyleClass().add("heart-button-modern");
        heartBtn.getStyleClass().add("heart-button-active");
        heartBtn.setText(""); // dùng graphic để tránh lỗi hiển thị
        heartBtn.setGraphic(buildHeartGraphic());
        heartBtn.setOnAction(e -> {
            favoritesService.removeFavorite(currentUserId, product.getId());
            // Refresh lại màn hình yêu thích để cập nhật danh sách
            show(stage);
            e.consume();
        });
        javafx.scene.layout.StackPane.setAlignment(heartBtn, javafx.geometry.Pos.TOP_RIGHT);
        javafx.scene.layout.StackPane.setMargin(heartBtn, new javafx.geometry.Insets(10, 10, 0, 0));
        imageContainer.getChildren().add(heartBtn);

        VBox infoBox = new VBox(10);
        infoBox.setPadding(new javafx.geometry.Insets(15, 15, 18, 15));
        infoBox.setPrefWidth(280);

        Label brandLabel = new Label(product.getBrand());
        brandLabel.getStyleClass().add("product-brand-modern");

        Label nameLabel = new Label(product.getNameVn());
        nameLabel.getStyleClass().add("product-name-modern");
        nameLabel.setWrapText(true);
        nameLabel.setPrefHeight(45);
        nameLabel.setMinHeight(45);

        HBox priceRow = new HBox(10);
        priceRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label priceLabel = new Label(String.format("%,d₫", product.getPrice().intValue()));
        priceLabel.getStyleClass().add("product-price-modern");
        if (product.getOriginalPrice() != null && product.getOriginalPrice().compareTo(product.getPrice()) > 0) {
            Text originalPrice = new Text(String.format("%,d₫", product.getOriginalPrice().intValue()));
            originalPrice.getStyleClass().add("original-price-modern");
            originalPrice.setStrikethrough(true);
            originalPrice.setStyle("-fx-font-size: 14px; -fx-fill: #999;");
            priceRow.getChildren().addAll(priceLabel, originalPrice);
        } else {
            priceRow.getChildren().add(priceLabel);
        }

        card.getChildren().addAll(imageContainer, infoBox);
        infoBox.getChildren().addAll(brandLabel, nameLabel, priceRow);

        card.setOnMouseClicked(e -> {
            ProductDetailController detailController = new ProductDetailController();
            detailController.show(product, stage);
        });

        return card;
    }

    private Text buildHeartGraphic() {
        Text icon = new Text(HEART_SOLID);
        icon.setStyle("-fx-font-size: 20px; -fx-font-family: 'Segoe UI Symbol', 'Segoe UI Emoji', 'Arial Unicode MS', 'Arial'; -fx-fill: white;");
        return icon;
    }

    private HBox createHeader(Stage stage) {
        HBox header = new HBox(15);
        header.getStyleClass().add("header");
        header.setPadding(new javafx.geometry.Insets(20, 30, 20, 30));

        Button backBtn = new Button("←");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> {
            HomeController homeController = new HomeController();
            homeController.show(stage);
        });

        Label title = new Label("Yêu thích");
        title.getStyleClass().add("page-title");
        HBox.setHgrow(title, Priority.ALWAYS);

        header.getChildren().addAll(backBtn, title);
        return header;
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
            if (i == 2) {
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
                    // đang ở Yêu thích
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
}



