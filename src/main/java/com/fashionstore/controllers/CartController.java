package com.fashionstore.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.fashionstore.services.CartService;
import com.fashionstore.dao.UserDAO;
import com.fashionstore.dao.ProductDAO;
import com.fashionstore.dao.VoucherDAO;
import com.fashionstore.models.CartItem;
import com.fashionstore.models.Voucher;
import com.fashionstore.dao.OrderDAO;
import java.math.BigDecimal;
import java.util.List;

public class CartController {
    private CartService cartService = new CartService();
    private ProductDAO productDAO = new ProductDAO();
    private VoucherDAO voucherDAO = new VoucherDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private int currentUserId = UserDAO.getCurrentUserId();
    private Voucher appliedVoucher = null;
    
    public void show(Stage stage) {
        VBox root = new VBox();
        root.getStyleClass().add("root");
        
        // Header
        HBox header = createHeader(stage);
        
        // Cart content
        ScrollPane scrollPane = new ScrollPane();
        HBox content = createCartContent(stage);
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent;");
        
        // Bottom bar
        HBox bottomBar = createBottomBar(stage);
        
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
        stage.setTitle("FashionStore - Giỏ hàng");
        stage.setMinWidth(1024);
        stage.setMinHeight(640);
        stage.show();
    }
    
    private HBox createHeader(Stage stage) {
        HBox header = new HBox(15);
        header.setPadding(new javafx.geometry.Insets(20, 30, 20, 30));
        header.getStyleClass().add("header");
        
        Button backBtn = new Button("←");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> {
            HomeController homeController = new HomeController();
            homeController.show(stage);
        });
        
        List<CartItem> cartItems = cartService.getCartItems(currentUserId);
        Label title = new Label("Giỏ hàng (" + cartItems.size() + ")");
        title.getStyleClass().add("page-title");
        HBox.setHgrow(title, Priority.ALWAYS);
        
        // User icon button
        Button userBtn = new Button("👤");
        userBtn.getStyleClass().add("icon-button");
        userBtn.setOnAction(e -> {
            UserInfoDialog.show(stage);
        });
        
        header.getChildren().addAll(backBtn, title, userBtn);
        return header;
    }
    
    private HBox createCartContent(Stage stage) {
        HBox content = new HBox(30);
        content.setPadding(new javafx.geometry.Insets(30, 40, 30, 40));
        
        // Left side - Cart items
        VBox itemsBox = new VBox(20);
        itemsBox.setPrefWidth(800);
        
        List<CartItem> cartItems = cartService.getCartItems(currentUserId);
        
        if (cartItems.isEmpty()) {
            VBox emptyCart = new VBox(20);
            emptyCart.setAlignment(javafx.geometry.Pos.CENTER);
            emptyCart.setPadding(new javafx.geometry.Insets(100));
            
            Label emptyLabel = new Label("Giỏ hàng");
            emptyLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #999; -fx-font-family: 'Arial', sans-serif;");
            
            Label emptyText = new Label("Giỏ hàng của bạn đang trống");
            emptyText.getStyleClass().add("empty-cart-text");
            
            Button shopBtn = new Button("Mua sắm ngay");
            shopBtn.getStyleClass().add("shop-now-button");
            shopBtn.setOnAction(e -> {
                HomeController homeController = new HomeController();
                homeController.show(stage);
            });
            
            emptyCart.getChildren().addAll(emptyLabel, emptyText, shopBtn);
            itemsBox.getChildren().add(emptyCart);
        } else {
            for (CartItem item : cartItems) {
                HBox cartItem = createCartItem(item, stage);
                itemsBox.getChildren().add(cartItem);
            }
            
            // Discount code section
            HBox discountSection = createDiscountSection(stage);
            itemsBox.getChildren().add(discountSection);
        }
        
        // Right side - Order summary
        VBox summaryCard = createOrderSummary(stage);
        summaryCard.setPrefWidth(400);
        
        content.getChildren().addAll(itemsBox, summaryCard);
        
        return content;
    }
    
    private HBox createCartItem(CartItem item, Stage stage) {
        HBox cartItem = new HBox(20);
        cartItem.getStyleClass().add("cart-item-modern");
        cartItem.setPadding(new javafx.geometry.Insets(20));
        
        // Make cart item clickable to navigate to product detail
        cartItem.setCursor(javafx.scene.Cursor.HAND);
        cartItem.setOnMouseClicked(e -> {
            ProductDetailController productDetailController = new ProductDetailController();
            productDetailController.show(item.getProduct(), stage);
        });
        
        // Product image
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefWidth(120);
        imageContainer.setPrefHeight(120);
        
        // Load product image if available
        String imagePath = item.getProduct().getImagePath();
        boolean imageLoaded = false;
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                String resourcePath = buildImageResourcePath(imagePath.trim());
                java.net.URL imgUrl = getClass().getResource(resourcePath);
                if (imgUrl != null) {
                    ImageView imageView = new ImageView(new Image(imgUrl.toExternalForm()));
                    imageView.setFitWidth(120);
                    imageView.setFitHeight(120);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    imageView.setCache(true);
                    imageContainer.getChildren().add(imageView);
                    imageLoaded = true;
                }
            } catch (Exception ex) {
                System.err.println("Error loading product image in cart: " + ex.getMessage());
            }
        }
        
        // Show placeholder if no image loaded
        if (!imageLoaded) {
            javafx.scene.shape.Rectangle placeholder = new javafx.scene.shape.Rectangle(120, 120);
            placeholder.getStyleClass().add("product-image-placeholder-modern");
            imageContainer.getChildren().add(placeholder);
        }
        
        // Product info
        VBox infoBox = new VBox(10);
        infoBox.setPrefWidth(400);
        
        Label name = new Label(item.getProduct().getNameVn());
        name.getStyleClass().add("cart-item-name-modern");
        
        String colorName = productDAO.getColorNameByCode(item.getProduct().getId(), item.getColor());
        Label details = new Label(item.getSize() + " • " + colorName);
        details.getStyleClass().add("cart-item-details-modern");
        
        Label price = new Label(formatPrice(item.getProduct().getPrice()));
        price.getStyleClass().add("cart-item-price-modern");
        
        infoBox.getChildren().addAll(name, details, price);
        
        // Quantity control
        HBox quantityBox = new HBox(15);
        quantityBox.setAlignment(javafx.geometry.Pos.CENTER);
        // Prevent click event from propagating to cart item
        quantityBox.setOnMouseClicked(javafx.event.Event::consume);
        
        Button minusBtn = new Button("-");
        minusBtn.getStyleClass().add("quantity-button-modern");
        minusBtn.setOnAction(e -> {
            if (item.getQuantity() > 1) {
                cartService.updateQuantity(item.getId(), item.getQuantity() - 1);
                show(stage);
            }
        });
        // Prevent click event from propagating
        minusBtn.setOnMouseClicked(javafx.event.Event::consume);
        
        Label quantity = new Label(String.valueOf(item.getQuantity()));
        quantity.getStyleClass().add("quantity-label-modern");
        
        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().add("quantity-button-modern");
        plusBtn.setOnAction(e -> {
            cartService.updateQuantity(item.getId(), item.getQuantity() + 1);
            show(stage);
        });
        // Prevent click event from propagating
        plusBtn.setOnMouseClicked(javafx.event.Event::consume);
        
        quantityBox.getChildren().addAll(minusBtn, quantity, plusBtn);
        
        // Subtotal
        Label subtotal = new Label(formatPrice(item.getSubtotal()));
        subtotal.getStyleClass().add("cart-item-subtotal-modern");
        subtotal.setPrefWidth(120);
        
        // Delete button
        Button deleteBtn = new Button("X");
        deleteBtn.getStyleClass().add("delete-button-modern");
        deleteBtn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial', sans-serif;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận");
            confirm.setHeaderText("Xóa sản phẩm");
            confirm.setContentText("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?");
            
            if (confirm.showAndWait().get() == ButtonType.OK) {
                cartService.removeFromCart(item.getId());
                show(stage);
            }
        });
        // Prevent click event from propagating
        deleteBtn.setOnMouseClicked(javafx.event.Event::consume);
        
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        cartItem.getChildren().addAll(imageContainer, infoBox, quantityBox, subtotal, deleteBtn);
        
        return cartItem;
    }
    
    private HBox createDiscountSection(Stage stage) {
        HBox section = new HBox(15);
        section.setPadding(new javafx.geometry.Insets(20));
        section.getStyleClass().add("discount-section-modern");
        
        Label title = new Label("Mã ưu đãi:");
        title.getStyleClass().add("discount-title-modern");
        
        ComboBox<Voucher> voucherCombo = new ComboBox<>();
        List<Voucher> availableVouchers = voucherDAO.getAllActiveVouchers();
        voucherCombo.getItems().add(null); // Add null option for "No voucher"
        voucherCombo.getItems().addAll(availableVouchers);
        
        // Set display text for vouchers
        voucherCombo.setCellFactory(param -> new ListCell<Voucher>() {
            @Override
            protected void updateItem(Voucher voucher, boolean empty) {
                super.updateItem(voucher, empty);
                if (empty || voucher == null) {
                    setText("Không chọn");
                } else {
                    String discountText = "";
                    if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
                        discountText = voucher.getDiscountValue() + "%";
                    } else {
                        discountText = formatPrice(voucher.getDiscountValue());
                    }
                    setText(voucher.getCode() + " - " + voucher.getDescriptionVn() + " (" + discountText + ")");
                }
            }
        });
        
        voucherCombo.setButtonCell(new ListCell<Voucher>() {
            @Override
            protected void updateItem(Voucher voucher, boolean empty) {
                super.updateItem(voucher, empty);
                if (empty || voucher == null) {
                    setText("Chọn mã giảm giá");
                } else {
                    String discountText = "";
                    if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
                        discountText = voucher.getDiscountValue() + "%";
                    } else {
                        discountText = formatPrice(voucher.getDiscountValue());
                    }
                    setText(voucher.getCode() + " - " + voucher.getDescriptionVn() + " (" + discountText + ")");
                }
            }
        });
        
        // Ensure the selected voucher instance comes from the current list
        if (appliedVoucher != null) {
            Voucher matched = availableVouchers.stream()
                    .filter(v -> v.getCode().equals(appliedVoucher.getCode()))
                    .findFirst()
                    .orElse(null);
            voucherCombo.setValue(matched);
        } else {
            voucherCombo.setValue(null);
        }

        voucherCombo.setConverter(new javafx.util.StringConverter<Voucher>() {
            @Override
            public String toString(Voucher voucher) {
                if (voucher == null) return "Chọn mã giảm giá";
                String discountText = voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE
                        ? voucher.getDiscountValue() + "%"
                        : formatPrice(voucher.getDiscountValue());
                return voucher.getCode() + " - " + voucher.getDescriptionVn() + " (" + discountText + ")";
            }

            @Override
            public Voucher fromString(String string) {
                return null;
            }
        });
        
        voucherCombo.getStyleClass().add("discount-input-modern");
        HBox.setHgrow(voucherCombo, Priority.ALWAYS);
        
        voucherCombo.setOnAction(e -> {
            Voucher selectedVoucher = voucherCombo.getValue();
            if (selectedVoucher == null) {
                // Remove voucher
                appliedVoucher = null;
                show(stage);
            } else {
                // Validate voucher
                if (!selectedVoucher.isValid()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi");
                    alert.setHeaderText(null);
                    alert.setContentText("Mã giảm giá đã hết hạn hoặc không còn hiệu lực!");
                    alert.showAndWait();
                    voucherCombo.setValue(null);
                    return;
                }
                
                BigDecimal subtotal = cartService.getCartTotal(currentUserId);
                if (subtotal.compareTo(selectedVoucher.getMinOrderAmount()) < 0) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Cảnh báo");
                    alert.setHeaderText(null);
                    alert.setContentText("Đơn hàng tối thiểu " + formatPrice(selectedVoucher.getMinOrderAmount()) + " để sử dụng mã này!");
                    alert.showAndWait();
                    voucherCombo.setValue(null);
                    return;
                }
                
                appliedVoucher = selectedVoucher;
                show(stage);
            }
        });
        
        section.getChildren().addAll(title, voucherCombo);
        return section;
    }
    
    private VBox createOrderSummary(Stage stage) {
        VBox summary = new VBox(20);
        summary.getStyleClass().add("order-summary-modern");
        summary.setPadding(new javafx.geometry.Insets(30));
        
        Label title = new Label("Tóm tắt đơn hàng");
        title.getStyleClass().add("summary-title-modern");
        
        BigDecimal originalTotal = cartService.getCartOriginalTotal(currentUserId);
        BigDecimal subtotal = cartService.getCartTotal(currentUserId);
        BigDecimal productDiscount = originalTotal.subtract(subtotal);
        BigDecimal shipping = cartService.calculateShippingFee(subtotal);
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        if (appliedVoucher != null) {
            voucherDiscount = appliedVoucher.calculateDiscount(subtotal);
        }
        BigDecimal total = cartService.calculateFinalTotal(currentUserId, voucherDiscount);
        
        List<CartItem> items = cartService.getCartItems(currentUserId);
        int itemCount = items.stream().mapToInt(CartItem::getQuantity).sum();
        
        // Original price row
        HBox originalPriceRow = new HBox();
        Label originalPriceLabel = new Label("Giá gốc (" + itemCount + " sản phẩm)");
        originalPriceLabel.getStyleClass().add("summary-label-modern");
        Label originalPriceValue = new Label(formatPrice(originalTotal));
        originalPriceValue.getStyleClass().add("summary-value-modern");
        HBox.setHgrow(originalPriceLabel, Priority.ALWAYS);
        originalPriceRow.getChildren().addAll(originalPriceLabel, originalPriceValue);
        
        // Product discount row
        HBox productDiscountRow = new HBox();
        Label productDiscountLabel = new Label("Giảm giá sản phẩm");
        productDiscountLabel.getStyleClass().add("summary-label-modern");
        Label productDiscountValue = new Label("-" + formatPrice(productDiscount));
        productDiscountValue.getStyleClass().add("discount-value-modern");
        HBox.setHgrow(productDiscountLabel, Priority.ALWAYS);
        productDiscountRow.getChildren().addAll(productDiscountLabel, productDiscountValue);

        // Voucher discount row (only if applied)
        HBox voucherDiscountRow = null;
        if (appliedVoucher != null) {
            voucherDiscountRow = new HBox();
            Label voucherDiscountLabel = new Label("Mã giảm giá (" + appliedVoucher.getCode() + ")");
            voucherDiscountLabel.getStyleClass().add("summary-label-modern");
            Label voucherDiscountValueLabel = new Label("-" + formatPrice(voucherDiscount));
            voucherDiscountValueLabel.getStyleClass().add("discount-value-modern");
            HBox.setHgrow(voucherDiscountLabel, Priority.ALWAYS);
            voucherDiscountRow.getChildren().addAll(voucherDiscountLabel, voucherDiscountValueLabel);
        }
        
        // Subtotal row (after discounts)
        HBox subtotalRow = new HBox();
        Label subtotalLabel = new Label("Tạm tính");
        subtotalLabel.getStyleClass().add("summary-label-modern");
        Label subtotalValue = new Label(formatPrice(subtotal));
        subtotalValue.getStyleClass().add("summary-value-modern");
        HBox.setHgrow(subtotalLabel, Priority.ALWAYS);
        subtotalRow.getChildren().addAll(subtotalLabel, subtotalValue);
        
        HBox shippingRow = new HBox();
        Label shippingLabel = new Label("Phí vận chuyển");
        shippingLabel.getStyleClass().add("summary-label-modern");
        Label shippingValue = new Label(formatPrice(shipping));
        shippingValue.getStyleClass().add("summary-value-modern");
        HBox.setHgrow(shippingLabel, Priority.ALWAYS);
        shippingRow.getChildren().addAll(shippingLabel, shippingValue);
        
        Separator separator = new Separator();
        
        HBox totalRow = new HBox();
        Label totalLabel = new Label("Tổng cộng");
        totalLabel.getStyleClass().add("total-label-large-modern");
        Label totalValue = new Label(formatPrice(total));
        totalValue.getStyleClass().add("total-value-modern");
        HBox.setHgrow(totalLabel, Priority.ALWAYS);
        totalRow.getChildren().addAll(totalLabel, totalValue);
        
        // Remove checkout button from summary - only show in bottom bar
        summary.getChildren().addAll(title, originalPriceRow, productDiscountRow);
        if (voucherDiscountRow != null) {
            summary.getChildren().add(voucherDiscountRow);
        }
        summary.getChildren().addAll(subtotalRow, shippingRow, separator, totalRow);
        
        return summary;
    }
    
    private HBox createBottomBar(Stage stage) {
        HBox bottomBar = new HBox(30);
        bottomBar.getStyleClass().add("cart-bottom-bar-modern");
        bottomBar.setPadding(new javafx.geometry.Insets(20, 40, 20, 40));
        
        List<CartItem> items = cartService.getCartItems(currentUserId);
        if (!items.isEmpty()) {
            VBox totalBox = new VBox(8);
            Label totalLabel = new Label("Tổng thanh toán");
            totalLabel.getStyleClass().add("total-label-modern");
            
            BigDecimal subtotal = cartService.getCartTotal(currentUserId);
            BigDecimal originalTotal = cartService.getCartOriginalTotal(currentUserId);
            BigDecimal productDiscount = originalTotal.subtract(subtotal);
            BigDecimal voucherDiscount = BigDecimal.ZERO;
            if (appliedVoucher != null) {
                voucherDiscount = appliedVoucher.calculateDiscount(subtotal);
            }
            final BigDecimal productDiscountFinal = productDiscount;
            final BigDecimal voucherDiscountFinal = voucherDiscount;
            BigDecimal total = cartService.calculateFinalTotal(currentUserId, voucherDiscountFinal);
            Label totalPrice = new Label(formatPrice(total));
            totalPrice.getStyleClass().add("total-price-modern");
            totalBox.getChildren().addAll(totalLabel, totalPrice);
            
            Button checkoutBtn = new Button("Thanh toán khi nhận hàng");
            checkoutBtn.getStyleClass().add("checkout-button-large-modern");
            checkoutBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận đặt hàng");
                confirm.setHeaderText("Thanh toán khi nhận hàng (COD)");
                confirm.setContentText("Bạn có chắc muốn đặt hàng với phương thức thanh toán khi nhận hàng?");
                
                if (confirm.showAndWait().get() == ButtonType.OK) {
                    BigDecimal shippingFee = cartService.calculateShippingFee(subtotal);
                    BigDecimal totalDiscount = productDiscountFinal.add(voucherDiscountFinal);
                    BigDecimal finalTotal = subtotal.add(shippingFee).subtract(voucherDiscountFinal);
                    
                    int orderId = orderDAO.createOrder(
                        currentUserId,
                        finalTotal,
                        shippingFee,
                        totalDiscount,
                        "Chờ thanh toán"
                    );
                    
                    if (orderId > 0) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Thành công");
                        alert.setHeaderText(null);
                        alert.setContentText("Đơn hàng #" + orderId + " đã được tạo. Bạn sẽ thanh toán khi nhận hàng.");
                        alert.showAndWait();
                        
                        // Clear cart after successful order
                        cartService.clearCart(currentUserId);
                        show(stage);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi");
                        alert.setHeaderText(null);
                        alert.setContentText("Không thể tạo đơn hàng. Vui lòng thử lại.");
                        alert.showAndWait();
                    }
                }
            });
            
            HBox.setHgrow(totalBox, Priority.ALWAYS);
            bottomBar.getChildren().addAll(totalBox, checkoutBtn);
        }
        
        return bottomBar;
    }
    
    private String formatPrice(BigDecimal price) {
        if (price == null) return "0₫";
        return String.format("%,d₫", price.intValue());
    }
    
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
