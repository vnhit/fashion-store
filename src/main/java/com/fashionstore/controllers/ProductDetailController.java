package com.fashionstore.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.fashionstore.models.Product;
import com.fashionstore.models.Review;
import com.fashionstore.services.CartService;
import com.fashionstore.services.FavoritesService;
import com.fashionstore.dao.UserDAO;
import com.fashionstore.dao.ReviewDAO;
import com.fashionstore.dao.ProductDAO;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProductDetailController {
    private CartService cartService = new CartService();
    private ReviewDAO reviewDAO = new ReviewDAO();
    private ProductDAO productDAO = new ProductDAO();
    private FavoritesService favoritesService = new FavoritesService();
    private int currentUserId = UserDAO.getCurrentUserId();
    private String selectedColor = "#1e3a5f";
    private String selectedSize = "M";
    // Dùng cùng một ký tự tim rỗng cho cả 2 trạng thái để tránh lỗi font ở ký tự tim đặc
    private static final String HEART_OUTLINE = "\u2661"; // ♡
    private static final String HEART_SOLID = "\u2661";   // ♡ (active sẽ phân biệt bằng màu nền)
    
    public void show(Product product, Stage stage) {
        // Refresh product to get latest rating and review count
        Product updatedProduct = productDAO.getProductById(product.getId());
        if (updatedProduct != null) {
            product = updatedProduct;
        }
        
        VBox root = new VBox();
        root.getStyleClass().add("root");
        
        // Header
        HBox header = createHeader(stage, product);
        
        // Product content
        ScrollPane scrollPane = new ScrollPane();
        VBox content = createProductContent(product, stage);
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent;");
        
        // Bottom bar
        HBox bottomBar = createBottomBar(product, stage);
        
        root.getChildren().addAll(header, scrollPane, bottomBar);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Đồng bộ kích thước với Home: phù hợp màn hình nhỏ, vẫn cho phóng to
        Scene scene = new Scene(root, 1200, 800);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        stage.setScene(scene);
        stage.setTitle("FashionStore - " + product.getNameVn());
        stage.setMinWidth(1024);
        stage.setMinHeight(640);
        stage.show();
    }
    
    private HBox createHeader(Stage stage, Product product) {
        HBox header = new HBox(15);
        header.setPadding(new javafx.geometry.Insets(15, 30, 15, 30));
        header.getStyleClass().add("header");
        
        Button backBtn = new Button("←");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> {
            ProductListController productListController = new ProductListController();
            productListController.show(stage);
        });
        
        HBox.setHgrow(backBtn, Priority.ALWAYS);
        
        // Heart button: thêm/xóa sản phẩm yêu thích
        Button heartBtn = new Button();
        heartBtn.getStyleClass().add("heart-button-modern");
        // Trạng thái ban đầu theo DB
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
        
        // User icon button
        Button userBtn = new Button("👤");
        userBtn.getStyleClass().add("icon-button");
        userBtn.setOnAction(e -> {
            UserInfoDialog.show(stage);
        });
        
        header.getChildren().addAll(backBtn, heartBtn, userBtn);
        return header;
    }
    
    private VBox createProductContent(Product product, Stage stage) {
        VBox mainContent = new VBox(30);
        mainContent.setPadding(new javafx.geometry.Insets(40, 50, 40, 50));
        
        // Top section: Product image and info
        HBox topSection = new HBox(40);
        
        // Product image
        VBox imageBox = new VBox();
        imageBox.setPrefWidth(500);
        
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("product-detail-image-modern");
        imageContainer.setPrefHeight(600);
        imageContainer.setPrefWidth(500);
        
        javafx.scene.shape.Rectangle placeholder = new javafx.scene.shape.Rectangle(500, 600);
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
                    imageView.setFitWidth(500);
                    imageView.setFitHeight(600);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
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
            StackPane.setMargin(badge, new javafx.geometry.Insets(20, 20, 0, 0));
            imageContainer.getChildren().add(badge);
        }
        
        imageBox.getChildren().add(imageContainer);
        
        // Product info
        VBox infoCard = new VBox(20);
        infoCard.getStyleClass().add("product-info-card-modern");
        infoCard.setPadding(new javafx.geometry.Insets(30));
        infoCard.setPrefWidth(700);
        
        HBox titleRow = new HBox();
        Label nameLabel = new Label(product.getNameVn());
        nameLabel.getStyleClass().add("product-detail-name-modern");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        VBox ratingBox = new VBox(5);
        ratingBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        double avgRating = reviewDAO.getAverageRating(product.getId());
        int reviewCount = reviewDAO.getReviewCount(product.getId());
        Label rating = new Label("⭐ " + String.format("%.1f", avgRating));
        rating.getStyleClass().add("product-rating-large-modern");
        Label reviewCountLabel = new Label("(" + reviewCount + " đánh giá)");
        reviewCountLabel.getStyleClass().add("review-count-modern");
        ratingBox.getChildren().addAll(rating, reviewCountLabel);
        
        titleRow.getChildren().addAll(nameLabel, ratingBox);
        
        Label brandLabel = new Label(product.getBrand());
        brandLabel.getStyleClass().add("product-brand-large");
        
        Label category = new Label("Nam giới - Mùa hè 2024");
        category.getStyleClass().add("product-category-modern");
        
        HBox priceRow = new HBox(15);
        priceRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label price = new Label(formatPrice(product.getPrice()));
        price.getStyleClass().add("product-detail-price-modern");
        
        if (product.getOriginalPrice() != null && product.getOriginalPrice().compareTo(product.getPrice()) > 0) {
            Text originalPrice = new Text(formatPrice(product.getOriginalPrice()));
            originalPrice.setStrikethrough(true);
            originalPrice.setStyle("-fx-font-size: 24px; -fx-fill: #999;");
            
            Label discount = new Label("-" + product.getDiscountPercent() + "%");
            discount.getStyleClass().add("discount-tag-modern");
            
            priceRow.getChildren().addAll(price, originalPrice, discount);
        } else {
            priceRow.getChildren().add(price);
        }
        
        Separator separator1 = new Separator();
        
        // Color selection
        VBox colorSection = createColorSection(product, stage);
        
        Separator separator2 = new Separator();
        
        // Size selection
        VBox sizeSection = createSizeSection(product, stage);
        
        Separator separator3 = new Separator();
        
        // Description
        VBox descSection = new VBox(15);
        Label descTitle = new Label("Mô tả sản phẩm:");
        descTitle.getStyleClass().add("section-title-small-modern");
        
        Label description = new Label(product.getDescriptionVn() != null ? product.getDescriptionVn() : 
            "Chất liệu 100% cotton thoáng mát, form dáng regular fit phù hợp mọi hoạt động. Thiết kế tối giản nhưng tinh tế, dễ dàng phối hợp với nhiều loại trang phục khác nhau. Công nghệ dệt sợi cao cấp giúp áo giữ form tốt sau nhiều lần giặt.");
        description.getStyleClass().add("product-description-modern");
        description.setWrapText(true);
        
        descSection.getChildren().addAll(descTitle, description);
        
        infoCard.getChildren().addAll(titleRow, brandLabel, category, priceRow, separator1, 
                                     colorSection, separator2, sizeSection, separator3, descSection);
        
        topSection.getChildren().addAll(imageBox, infoCard);
        
        // Reviews section
        VBox reviewsSection = createReviewsSection(product, stage);
        
        mainContent.getChildren().addAll(topSection, reviewsSection);
        
        return mainContent;
    }
    
    private VBox createColorSection(Product product, Stage stage) {
        VBox colorSection = new VBox(15);
        Label colorLabel = new Label("Màu sắc:");
        colorLabel.getStyleClass().add("option-label-modern");
        
        HBox colorBox = new HBox(15);
        String[] colors = {"#1e3a5f", "#ffffff", "#e0e0e0", "#8b4513"};
        String[] colorNames = {"Xanh Navy", "Trắng", "Xám", "Nâu"};
        
        for (int i = 0; i < colors.length; i++) {
            VBox colorOption = new VBox(5);
            colorOption.setAlignment(javafx.geometry.Pos.CENTER);
            
            Circle colorCircle = new Circle(30);
            if (colors[i].equals(selectedColor)) {
                colorCircle.setStyle("-fx-fill: " + colors[i] + "; -fx-stroke: #2196F3; -fx-stroke-width: 3;");
            } else {
                colorCircle.setStyle("-fx-fill: " + colors[i] + "; -fx-stroke: #ddd; -fx-stroke-width: 2;");
            }
            
            final String colorCode = colors[i];
            colorCircle.setOnMouseClicked(e -> {
                selectedColor = colorCode;
                show(product, stage);
            });
            
            Label colorName = new Label(colorNames[i]);
            colorName.getStyleClass().add("color-name-label");
            
            colorOption.getChildren().addAll(colorCircle, colorName);
            colorBox.getChildren().add(colorOption);
        }
        colorSection.getChildren().addAll(colorLabel, colorBox);
        return colorSection;
    }
    
    private VBox createSizeSection(Product product, Stage stage) {
        VBox sizeSection = new VBox(15);
        HBox sizeHeader = new HBox();
        Label sizeLabel = new Label("Kích cỡ:");
        sizeLabel.getStyleClass().add("option-label-modern");
        HBox.setHgrow(sizeLabel, Priority.ALWAYS);
        
        Hyperlink sizeChart = new Hyperlink("Bảng size");
        sizeChart.getStyleClass().add("size-chart-link-modern");
        sizeHeader.getChildren().addAll(sizeLabel, sizeChart);
        
        HBox sizeBox = new HBox(15);
        String[] sizes;
        if (product.getSizes() != null && !product.getSizes().isEmpty()) {
            sizes = new java.util.LinkedHashSet<>(product.getSizes()).toArray(new String[0]); // remove duplicates, keep order
        } else {
            sizes = new String[]{"S", "M", "L", "XL", "2XL"};
        }
        
        for (String size : sizes) {
            Button sizeBtn = new Button(size);
            if (size.equals(selectedSize)) {
                sizeBtn.getStyleClass().add("size-button-active-modern");
            } else {
                sizeBtn.getStyleClass().add("size-button-modern");
            }
            
            final String sizeValue = size;
            sizeBtn.setOnAction(e -> {
                selectedSize = sizeValue;
                show(product, stage);
            });
            
            sizeBox.getChildren().add(sizeBtn);
        }
        sizeSection.getChildren().addAll(sizeHeader, sizeBox);
        return sizeSection;
    }
    
    private VBox createReviewsSection(Product product, Stage stage) {
        VBox reviewsSection = new VBox(20);
        reviewsSection.setPadding(new javafx.geometry.Insets(30));
        reviewsSection.getStyleClass().add("reviews-section-modern");
        
        Label sectionTitle = new Label("Đánh giá và Bình luận");
        sectionTitle.getStyleClass().add("section-title-modern");
        sectionTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Arial', sans-serif;");
        
        // Add review form
        VBox reviewForm = createReviewForm(product, stage);
        
        // Reviews list
        VBox reviewsList = createReviewsList(product);
        
        reviewsSection.getChildren().addAll(sectionTitle, reviewForm, reviewsList);
        
        return reviewsSection;
    }
    
    private VBox createReviewForm(Product product, Stage stage) {
        VBox form = new VBox(15);
        form.setPadding(new javafx.geometry.Insets(20));
        form.getStyleClass().add("review-form-modern");
        form.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8;");
        
        Label formTitle = new Label("Viết đánh giá của bạn");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Arial', sans-serif;");
        
        // Rating selection
        HBox ratingBox = new HBox(10);
        ratingBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label ratingLabel = new Label("Đánh giá:");
        ratingLabel.setStyle("-fx-font-size: 14px; -fx-font-family: 'Arial', sans-serif;");
        
        HBox starsBox = new HBox(5);
        int[] selectedRating = {0};
        
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            Button starBtn = new Button("☆");
            starBtn.setStyle("-fx-font-size: 24px; -fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;");
            starBtn.setOnAction(e -> {
                selectedRating[0] = rating;
                updateStarButtons(starsBox, rating);
            });
            starsBox.getChildren().add(starBtn);
        }
        
        ratingBox.getChildren().addAll(ratingLabel, starsBox);
        
        // Comment text area
        Label commentLabel = new Label("Bình luận:");
        commentLabel.setStyle("-fx-font-size: 14px; -fx-font-family: 'Arial', sans-serif;");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Chia sẻ trải nghiệm của bạn về sản phẩm này...");
        commentArea.setPrefRowCount(4);
        commentArea.setWrapText(true);
        commentArea.setStyle("-fx-font-size: 14px; -fx-font-family: 'Arial', sans-serif;");
        
        // Submit button
        Button submitBtn = new Button("Gửi đánh giá");
        submitBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> {
            if (selectedRating[0] == 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cảnh báo");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng chọn số sao đánh giá!");
                alert.showAndWait();
                return;
            }
            
            Review review = new Review(product.getId(), currentUserId, selectedRating[0], commentArea.getText());
            if (reviewDAO.addReview(review)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đánh giá của bạn đã được gửi!");
                alert.showAndWait();
                
                // Refresh the page
                Product updatedProduct = productDAO.getProductById(product.getId());
                if (updatedProduct != null) {
                    show(updatedProduct, stage);
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Không thể gửi đánh giá. Vui lòng thử lại!");
                alert.showAndWait();
            }
        });
        
        form.getChildren().addAll(formTitle, ratingBox, commentLabel, commentArea, submitBtn);
        
        return form;
    }
    
    private void updateStarButtons(HBox starsBox, int rating) {
        for (int i = 0; i < starsBox.getChildren().size(); i++) {
            Button starBtn = (Button) starsBox.getChildren().get(i);
            if (i < rating) {
                starBtn.setText("★");
                starBtn.setStyle("-fx-font-size: 24px; -fx-text-fill: #FFD700; -fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;");
            } else {
                starBtn.setText("☆");
                starBtn.setStyle("-fx-font-size: 24px; -fx-text-fill: #ccc; -fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;");
            }
        }
    }
    
    private VBox createReviewsList(Product product) {
        VBox reviewsList = new VBox(15);
        
        List<Review> reviews = reviewDAO.getReviewsByProductId(product.getId());
        
        if (reviews.isEmpty()) {
            Label noReviews = new Label("Chưa có đánh giá nào. Hãy là người đầu tiên đánh giá sản phẩm này!");
            noReviews.setStyle("-fx-font-size: 14px; -fx-text-fill: #999; -fx-font-family: 'Arial', sans-serif; -fx-padding: 20;");
            reviewsList.getChildren().add(noReviews);
        } else {
            for (Review review : reviews) {
                VBox reviewCard = createReviewCard(review);
                reviewsList.getChildren().add(reviewCard);
            }
        }
        
        return reviewsList;
    }
    
    private VBox createReviewCard(Review review) {
        VBox reviewCard = new VBox(10);
        reviewCard.setPadding(new javafx.geometry.Insets(15));
        reviewCard.getStyleClass().add("review-card-modern");
        reviewCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");
        
        // Header: User name and rating
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label userName = new Label(review.getUserName() != null ? review.getUserName() : "Người dùng");
        userName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial', sans-serif;");
        
        HBox stars = new HBox(3);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= review.getRating() ? "★" : "☆");
            star.setStyle("-fx-font-size: 16px; -fx-text-fill: " + (i <= review.getRating() ? "#FFD700" : "#ccc") + ";");
            stars.getChildren().add(star);
        }
        
        HBox.setHgrow(userName, Priority.ALWAYS);
        header.getChildren().addAll(userName, stars);
        
        // Comment
        if (review.getComment() != null && !review.getComment().trim().isEmpty()) {
            Label comment = new Label(review.getComment());
            comment.setWrapText(true);
            comment.setStyle("-fx-font-size: 14px; -fx-font-family: 'Arial', sans-serif; -fx-text-fill: #333;");
            reviewCard.getChildren().add(comment);
        }
        
        // Date
        if (review.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            Label dateLabel = new Label(review.getCreatedAt().format(formatter));
            dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999; -fx-font-family: 'Arial', sans-serif;");
            reviewCard.getChildren().add(dateLabel);
        }
        
        reviewCard.getChildren().add(0, header);
        
        return reviewCard;
    }
    
    private HBox createBottomBar(Product product, Stage stage) {
        HBox bottomBar = new HBox(30);
        bottomBar.getStyleClass().add("bottom-bar-modern");
        bottomBar.setPadding(new javafx.geometry.Insets(20, 40, 20, 40));
        
        VBox totalBox = new VBox(8);
        Label totalLabel = new Label("Tổng cộng");
        totalLabel.getStyleClass().add("total-label-modern");
        Label totalPrice = new Label(formatPrice(product.getPrice()));
        totalPrice.getStyleClass().add("total-price-modern");
        totalBox.getChildren().addAll(totalLabel, totalPrice);
        
        HBox buttonBox = new HBox(15);
        
        Button addToCartBtn = new Button("🛍️ Thêm vào giỏ");
        addToCartBtn.getStyleClass().add("add-to-cart-large-button-modern");
        addToCartBtn.setOnAction(e -> {
            boolean success = cartService.addToCart(currentUserId, product.getId(), selectedSize, selectedColor, 1);
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đã thêm sản phẩm vào giỏ hàng!");
                alert.showAndWait();
            }
        });
        
        Button buyNowBtn = new Button("Mua ngay →");
        buyNowBtn.getStyleClass().add("buy-now-button-modern");
        buyNowBtn.setOnAction(e -> {
            cartService.addToCart(currentUserId, product.getId(), selectedSize, selectedColor, 1);
            CartController cartController = new CartController();
            cartController.show(stage);
        });
        
        buttonBox.getChildren().addAll(addToCartBtn, buyNowBtn);
        
        HBox.setHgrow(totalBox, Priority.ALWAYS);
        bottomBar.getChildren().addAll(totalBox, buttonBox);
        
        return bottomBar;
    }
    
    // Cập nhật trạng thái nút trái tim dựa theo việc sản phẩm có đang ở danh sách yêu thích hay không
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
}
