package com.fashionstore.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.fashionstore.dao.UserDAO;
import com.fashionstore.dao.OrderDAO;
import com.fashionstore.dao.VoucherDAO;
import com.fashionstore.dao.ReviewDAO;
import com.fashionstore.models.User;
import com.fashionstore.models.Order;
import com.fashionstore.models.Voucher;
import com.fashionstore.models.Review;
import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AccountController {
    private UserDAO userDAO = new UserDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private int currentUserId = UserDAO.getCurrentUserId();
    private VoucherDAO voucherDAO = new VoucherDAO();
    private ReviewDAO reviewDAO = new ReviewDAO();
    
    public void show(Stage stage) {
        VBox root = new VBox();
        root.getStyleClass().add("root");
        
        // Header
        HBox header = createHeader(stage);
        
        // Account content
        ScrollPane scrollPane = new ScrollPane();
        VBox content = createAccountContent(stage);
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent;");
        
        // Bottom Navigation
        HBox bottomNav = createBottomNavigation(stage);
        
        root.getChildren().addAll(header, scrollPane, bottomNav);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Đồng bộ kích thước với Home: phù hợp màn hình nhỏ, vẫn cho phóng to
        Scene scene = new Scene(root, 1200, 800);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        stage.setScene(scene);
        stage.setTitle("FashionStore - Tài khoản");
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
        
        Label title = new Label("Tài khoản");
        title.getStyleClass().add("page-title");
        HBox.setHgrow(title, Priority.ALWAYS);
        
        // Bỏ nút cài đặt theo yêu cầu
        header.getChildren().addAll(backBtn, title);
        return header;
    }
    
    private VBox createAccountContent(Stage stage) {
        VBox content = new VBox(25);
        content.setPadding(new javafx.geometry.Insets(30, 40, 40, 40));
        
        // Profile section
        VBox profileSection = createProfileSection(stage);
        
        // Points section
        VBox pointsSection = createPointsSection();
        
        // Orders section
        VBox ordersSection = createOrdersSection(stage);
        
        // Account menu
        VBox menuSection = createMenuSection(stage);
        
        // Language section
        HBox languageSection = createLanguageSection();
        
        // Logout button
        Button logoutBtn = new Button("Đăng xuất");
        logoutBtn.getStyleClass().add("logout-button-modern");
        logoutBtn.setOnAction(e -> {
            // Xóa thông tin người dùng đang đăng nhập và quay lại màn hình đăng nhập
            UserDAO.setCurrentUserId(0);
            LoginController loginController = new LoginController();
            loginController.show(stage);
        });
        
        // Version
        Label version = new Label("Phiên bản 2.4.0");
        version.getStyleClass().add("version-label-modern");
        
        content.getChildren().addAll(profileSection, pointsSection, ordersSection, 
                                     menuSection, languageSection, logoutBtn, version);
        
        return content;
    }
    
    private VBox createProfileSection(Stage stage) {
        VBox section = new VBox(15);
        section.setAlignment(javafx.geometry.Pos.CENTER);
        section.setPadding(new javafx.geometry.Insets(30));
        
        StackPane avatarContainer = new StackPane();
        avatarContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Background circle
        javafx.scene.shape.Circle avatarBg = new javafx.scene.shape.Circle(70);
        avatarBg.getStyleClass().add("avatar-circle-modern");
        avatarContainer.getChildren().add(avatarBg);
        
        // Avatar image view
        ImageView avatarImageView = new ImageView();
        avatarImageView.setFitWidth(140);
        avatarImageView.setFitHeight(140);
        avatarImageView.setPreserveRatio(false);
        avatarImageView.setSmooth(true);
        avatarImageView.setCache(true);
        
        // Create circular clip for avatar - center it properly
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(70, 70, 70);
        avatarImageView.setClip(clip);
        
        // Load user avatar if exists
        User user = userDAO.getUserById(currentUserId);
        if (user != null && user.getAvatarPath() != null && !user.getAvatarPath().isEmpty()) {
            try {
                File avatarFile = new File(user.getAvatarPath());
                if (avatarFile.exists()) {
                    Image avatarImage = new Image(avatarFile.toURI().toString(), 140, 140, false, true);
                    avatarImageView.setImage(avatarImage);
                }
            } catch (Exception e) {
                System.err.println("Error loading avatar: " + e.getMessage());
            }
        }
        
        StackPane.setAlignment(avatarImageView, javafx.geometry.Pos.CENTER);
        avatarContainer.getChildren().add(avatarImageView);
        
        Button editBtn = new Button("...");
        editBtn.getStyleClass().add("edit-avatar-button-modern");
        editBtn.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Arial', sans-serif;");
        editBtn.setOnAction(e -> {
            EditUserDialog editDialog = new EditUserDialog(() -> {
                // Refresh account page after save
                show(stage);
            });
            editDialog.show(stage);
        });
        StackPane.setAlignment(editBtn, javafx.geometry.Pos.BOTTOM_RIGHT);
        StackPane.setMargin(editBtn, new javafx.geometry.Insets(0, -10, -10, 0));
        avatarContainer.getChildren().add(editBtn);
        
        // Nếu không có user, hiển thị là "Khách"
        Label name = new Label(user != null ? user.getName() : "Khách");
        name.getStyleClass().add("user-name-modern");
        
        Label membership = new Label(user != null ? "Thành viên " + user.getMembershipLevel() : "Thành viên Vàng");
        membership.getStyleClass().add("membership-label-modern");
        
        section.getChildren().addAll(avatarContainer, name, membership);
        return section;
    }
    
    private VBox createPointsSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("points-card-modern");
        section.setPadding(new javafx.geometry.Insets(25));
        
        HBox pointsHeader = new HBox(10);
        Label starIcon = new Label("⭐");
        starIcon.setStyle("-fx-font-size: 24px; -fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', 'Arial Unicode MS', 'Arial';");
        Label pointsLabel = new Label("Điểm tích lũy");
        pointsLabel.getStyleClass().add("points-label-modern");
        HBox.setHgrow(pointsLabel, Priority.ALWAYS);
        
        User user = userDAO.getUserById(currentUserId);
        Label pointsValue = new Label((user != null ? user.getPoints() : 750) + " điểm");
        pointsValue.getStyleClass().add("points-value-modern");
        pointsHeader.getChildren().addAll(starIcon, pointsLabel, pointsValue);
        
        ProgressBar progressBar = new ProgressBar(0.75);
        progressBar.getStyleClass().add("points-progress-modern");
        progressBar.setPrefHeight(10);
        
        HBox progressLabels = new HBox();
        Label silver = new Label("Thành viên Bạc");
        silver.getStyleClass().add("progress-label-modern");
        Label diamond = new Label("1000 để lên Kim Cương");
        diamond.getStyleClass().add("progress-label-modern");
        HBox.setHgrow(silver, Priority.ALWAYS);
        progressLabels.getChildren().addAll(silver, diamond);
        
        section.getChildren().addAll(pointsHeader, progressBar, progressLabels);
        return section;
    }
    
    private VBox createOrdersSection(Stage stage) {
        VBox section = new VBox(20);
        section.getStyleClass().add("orders-card-modern");
        section.setPadding(new javafx.geometry.Insets(25));
        
        HBox header = new HBox();
        Label title = new Label("Đơn hàng của tôi");
        title.getStyleClass().add("section-title-modern");
        HBox.setHgrow(title, Priority.ALWAYS);
        // Bỏ "Xem tất cả →" theo yêu cầu, chỉ giữ lại tiêu đề
        header.getChildren().addAll(title);
        
        HBox orderStatuses = new HBox(25);
        orderStatuses.setAlignment(javafx.geometry.Pos.CENTER);
        
        String[] statuses = {"Chờ thanh toán", "Chờ lấy hàng", "Đang giao", "Đánh giá"};
        // Dùng emoji trực tiếp
        String[] icons = {"💼", "📦", "🚚", "💬"};
        // Map trạng thái hiển thị sang trạng thái trong database
        String[] dbStatuses = {"Chờ thanh toán", "Đã nhận", "Đang giao", "Hoàn thành"};
        
        // Tính số lượng badge cho từng trạng thái
        // 0: Chờ thanh toán  -> đếm đơn ở trạng thái "Chờ thanh toán"
        // 1: Chờ lấy hàng    -> đếm đơn ở trạng thái "Đã nhận"
        // 2: Đang giao       -> đếm đơn ở trạng thái "Đang giao"
        // 3: Đánh giá        -> đếm số đánh giá mà người dùng đã viết
        int[] badges = new int[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            if (i == 3) {
                // Badge "Đánh giá" phản ánh đúng số lượng review hiện có của user
                badges[i] = reviewDAO.getReviewsByUserId(currentUserId).size();
            } else {
                badges[i] = orderDAO.getOrderCountByUserAndStatus(currentUserId, dbStatuses[i]);
            }
        }
        
        for (int i = 0; i < statuses.length; i++) {
            VBox statusItem = new VBox(10);
            statusItem.setAlignment(javafx.geometry.Pos.CENTER);
            
            StackPane iconContainer = new StackPane();
            iconContainer.setPrefWidth(60);
            iconContainer.setPrefHeight(60);
            iconContainer.setMinWidth(60);
            iconContainer.setMinHeight(60);
            iconContainer.setAlignment(javafx.geometry.Pos.CENTER);
            
            // Dùng Label với cấu hình để hiển thị emoji
            Label iconLabel = new Label(icons[i]);
            // Đảm bảo Label có text và không bị ẩn
            iconLabel.setText(icons[i]);
            iconLabel.setStyle(
                "-fx-font-size: 40px !important; " +
                "-fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', 'Arial Unicode MS', 'Arial', 'MS Gothic', 'Malgun Gothic', sans-serif !important; " +
                "-fx-text-fill: #333333 !important; " +
                "-fx-alignment: center !important; " +
                "-fx-content-display: center !important; " +
                "-fx-background-color: transparent;"
            );
            iconLabel.setPrefWidth(60);
            iconLabel.setPrefHeight(60);
            iconLabel.setMinWidth(60);
            iconLabel.setMinHeight(60);
            iconLabel.setMaxWidth(60);
            iconLabel.setMaxHeight(60);
            iconLabel.setAlignment(javafx.geometry.Pos.CENTER);
            iconLabel.setContentDisplay(javafx.scene.control.ContentDisplay.CENTER);
            iconLabel.setWrapText(false);
            StackPane.setAlignment(iconLabel, javafx.geometry.Pos.CENTER);
            iconContainer.getChildren().add(iconLabel);
            
            if (badges[i] > 0) {
                Label badge = new Label(String.valueOf(badges[i]));
                badge.getStyleClass().add("order-badge-modern");
                StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_RIGHT);
                StackPane.setMargin(badge, new javafx.geometry.Insets(5, 50, 0, 0));
                iconContainer.getChildren().add(badge);
            }
            
            Label label = new Label(statuses[i]);
            label.getStyleClass().add("order-status-label-modern");
            label.setAlignment(javafx.geometry.Pos.CENTER);
            
            statusItem.getChildren().addAll(iconContainer, label);
            HBox.setHgrow(statusItem, Priority.ALWAYS);
            
            // Thêm event handler cho toàn bộ status item (chỉ gắn 1 nơi để tránh bị gọi đúp)
            final int statusIdx = i;
            statusItem.setOnMouseClicked(e -> {
                handleOrderStatusClick(stage, statusIdx, statuses[statusIdx]);
            });
            
            orderStatuses.getChildren().add(statusItem);
        }
        
        section.getChildren().addAll(header, orderStatuses);
        return section;
    }
    
    private VBox createMenuSection(Stage stage) {
        VBox section = new VBox(10);
        section.getStyleClass().add("menu-card-modern");
        section.setPadding(new javafx.geometry.Insets(20));
        
        String[] menuItems = {"Sổ địa chỉ", "Ví Voucher", "Phương thức thanh toán", "Thông báo"};
        String[] icons = {"📍", "🎫", "💳", "🔔"};
        boolean[] hasNew = {false, true, false, false};
        
        for (int i = 0; i < menuItems.length; i++) {
            HBox menuItem = new HBox(15);
            menuItem.getStyleClass().add("menu-item-modern");
            menuItem.setPadding(new javafx.geometry.Insets(15));
            
            Label iconLabel = new Label(icons[i]);
            iconLabel.setStyle("-fx-font-size: 22px; -fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', 'Arial Unicode MS', 'Arial';");
            
            Label label = new Label(menuItems[i]);
            label.getStyleClass().add("menu-label-modern");
            HBox.setHgrow(label, Priority.ALWAYS);
            
            HBox rightSide = new HBox(10);
            if (hasNew[i]) {
                Label newBadge = new Label("Mới");
                newBadge.getStyleClass().add("new-badge-modern");
                rightSide.getChildren().add(newBadge);
            }
            
            // Bỏ mũi tên "→" theo yêu cầu
            menuItem.getChildren().addAll(iconLabel, label, rightSide);
            
            // Thêm event handler để có thể thao tác
            final int index = i;
            menuItem.setOnMouseClicked(e -> {
                handleMenuClick(index, menuItems[index], stage);
            });
            
            section.getChildren().add(menuItem);
        }
        
        return section;
    }
    
    private void handleMenuClick(int index, String menuName, Stage stage) {
        if (index == 0) {
            // Sổ địa chỉ
            AddressBookController addressBookController = new AddressBookController();
            addressBookController.show(stage);
        } else if (index == 1) {
            // Ví Voucher - hiển thị các voucher đang hoạt động mà người dùng có thể sử dụng
            showVoucherWallet(stage);
        } else {
            System.out.println("Clicked: " + menuName);
            // Có thể thêm logic xử lý cho các menu item khác ở đây
        }
    }
    
    private HBox createLanguageSection() {
        HBox section = new HBox(15);
        section.getStyleClass().add("language-section-modern");
        section.setPadding(new javafx.geometry.Insets(20));
        
        Label icon = new Label("🌐");
        icon.setStyle("-fx-font-size: 22px; -fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', 'Arial Unicode MS', 'Arial';");
        
        Label label = new Label("Ngôn ngữ");
        label.getStyleClass().add("menu-label-modern");
        HBox.setHgrow(label, Priority.ALWAYS);
        
        Label language = new Label("Tiếng Việt");
        language.getStyleClass().add("language-value-modern");
        // Bỏ mũi tên "→" theo yêu cầu
        section.getChildren().addAll(icon, label, language);
        
        // Thêm event handler để có thể thao tác
        section.setOnMouseClicked(e -> {
            handleLanguageClick();
        });
        
        return section;
    }
    
    private void handleOrderStatusClick(Stage stage, int index, String status) {
        System.out.println("Clicked order status: " + status);
        // 0: Chờ thanh toán  -> mở giỏ hàng
        // 1: Chờ lấy hàng    -> các đơn hàng đã được admin xác nhận "Đã nhận"
        // 2: Đang giao       -> các đơn hàng đang giao
        // 3: Đánh giá        -> các sản phẩm mà người dùng đã đánh giá
        switch (index) {
            case 0:
                // Điều hướng sang màn hình giỏ hàng
                CartController cartController = new CartController();
                cartController.show(stage);
                break;
            case 1:
                // Đơn "Chờ lấy hàng" = các đơn đã được admin xác nhận "Đã nhận"
                showOrdersForStatus(stage, "Đã nhận", "Đơn chờ lấy hàng", "Đã nhận");
                break;
            case 2:
                // Đơn "Đang giao"
                showOrdersForStatus(stage, "Đang giao", "Đơn đang giao", "Đang giao");
                break;
            case 3:
                // Các sản phẩm người dùng đã đánh giá
                showUserReviews(stage);
                break;
            default:
                break;
        }
    }

    /**
     * Hiển thị danh sách đơn hàng của người dùng hiện tại theo trạng thái
     * với giao diện từng đơn giống card sản phẩm trong giỏ hàng, kèm trạng thái.
     */
    private void showOrdersForStatus(Stage owner, String dbStatus, String title, String displayStatus) {
        List<Order> orders = orderDAO.getOrdersByUserAndStatus(currentUserId, dbStatus);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initOwner(owner);

        VBox root = new VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));

        if (orders.isEmpty()) {
            Label empty = new Label("Không có đơn hàng ở trạng thái này.");
            empty.getStyleClass().add("empty-cart-text");
            root.getChildren().add(empty);
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Order o : orders) {
                HBox orderItem = new HBox(20);
                orderItem.getStyleClass().add("cart-item-modern");
                orderItem.setPadding(new javafx.geometry.Insets(20));

                // Bên trái: placeholder ảnh (giống cart)
                StackPane imageContainer = new StackPane();
                imageContainer.setPrefWidth(80);
                imageContainer.setPrefHeight(80);
                javafx.scene.shape.Rectangle placeholder = new javafx.scene.shape.Rectangle(80, 80);
                placeholder.getStyleClass().add("product-image-placeholder-modern");
                imageContainer.getChildren().add(placeholder);

                // Giữa: thông tin đơn
                VBox infoBox = new VBox(8);
                Label orderTitle = new Label("Đơn hàng #" + o.getId());
                orderTitle.getStyleClass().add("cart-item-name-modern");

                String created = o.getCreatedAt() != null ? o.getCreatedAt().format(fmt) : "";
                Label createdLabel = new Label(created);
                createdLabel.getStyleClass().add("cart-item-details-modern");

                Label totalLabel = new Label("Tổng: " + formatPrice(o.getTotalAmount()));
                totalLabel.getStyleClass().add("cart-item-price-modern");

                infoBox.getChildren().addAll(orderTitle, createdLabel, totalLabel);

                // Bên phải: trạng thái
                Label statusLabel = new Label(displayStatus);
                statusLabel.getStyleClass().add("order-status-label-modern");
                statusLabel.setMinWidth(100);

                HBox.setHgrow(infoBox, Priority.ALWAYS);
                orderItem.getChildren().addAll(imageContainer, infoBox, statusLabel);

                root.getChildren().add(orderItem);
            }
        }

        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    /**
     * Hiển thị danh sách các đánh giá mà người dùng hiện tại đã viết.
     */
    private void showUserReviews(Stage owner) {
        List<Review> reviews = reviewDAO.getReviewsByUserId(currentUserId);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Đánh giá của tôi");
        dialog.initOwner(owner);

        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(15));

        if (reviews.isEmpty()) {
            Label empty = new Label("Bạn chưa viết đánh giá nào.");
            empty.getStyleClass().add("empty-cart-text");
            root.getChildren().add(empty);
        } else {
            ListView<String> listView = new ListView<>();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Review r : reviews) {
                String created = r.getCreatedAt() != null ? r.getCreatedAt().format(fmt) : "";
                String productName = r.getProductName() != null ? r.getProductName() : "Sản phẩm #" + r.getProductId();
                String line = productName + " - " + r.getRating() + "★ - " + created + " - " + r.getComment();
                listView.getItems().add(line);
            }
            root.getChildren().add(listView);
        }

        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    /**
     * Hiển thị "Ví Voucher" – các voucher đang hoạt động mà người dùng có thể dùng.
     */
    private void showVoucherWallet(Stage owner) {
        List<Voucher> vouchers = voucherDAO.getAllActiveVouchers();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Ví Voucher của tôi");
        dialog.initOwner(owner);

        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(15));

        if (vouchers.isEmpty()) {
            Label empty = new Label("Hiện bạn chưa có mã giảm giá nào khả dụng.");
            empty.getStyleClass().add("empty-cart-text");
            root.getChildren().add(empty);
        } else {
            ListView<String> listView = new ListView<>();
            for (Voucher v : vouchers) {
                String discountText;
                if (v.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
                    discountText = v.getDiscountValue().stripTrailingZeros().toPlainString() + "%";
                } else {
                    discountText = formatPrice(v.getDiscountValue());
                }

                String line = v.getCode() + " - " +
                        (v.getDescriptionVn() != null ? v.getDescriptionVn() : "") +
                        " (" + discountText + ", tối thiểu " + formatPrice(v.getMinOrderAmount()) + ")";
                listView.getItems().add(line);
            }
            root.getChildren().add(listView);
        }

        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0₫";
        return String.format("%,d₫", price.intValue());
    }
    
    private void handleLanguageClick() {
        System.out.println("Clicked language section");
        // Có thể thêm logic chuyển đổi ngôn ngữ ở đây
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
            if (i == 3) {
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
}
