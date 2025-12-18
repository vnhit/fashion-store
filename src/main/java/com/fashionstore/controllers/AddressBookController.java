package com.fashionstore.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.fashionstore.dao.AddressDAO;
import com.fashionstore.dao.UserDAO;
import com.fashionstore.models.Address;

public class AddressBookController {
    private AddressDAO addressDAO = new AddressDAO();
    private int currentUserId = UserDAO.getCurrentUserId();
    
    public void show(Stage stage) {
        VBox root = new VBox();
        root.getStyleClass().add("root");
        
        // Header
        HBox header = createHeader(stage);
        
        // Content
        ScrollPane scrollPane = new ScrollPane();
        VBox content = createContent(stage);
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent;");
        
        // Bottom Navigation
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
        stage.setTitle("FashionStore - Sổ địa chỉ");
        stage.setMinWidth(1024);
        stage.setMinHeight(640);
        stage.show();
    }
    
    private HBox createHeader(Stage stage) {
        HBox header = new HBox(15);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.getStyleClass().add("header");
        
        Button backBtn = new Button("←");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> {
            AccountController accountController = new AccountController();
            accountController.show(stage);
        });
        
        Label title = new Label("Sổ địa chỉ");
        title.getStyleClass().add("page-title");
        HBox.setHgrow(title, Priority.ALWAYS);
        
        Button addBtn = new Button("➕");
        addBtn.getStyleClass().add("icon-button");
        addBtn.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', 'Arial Unicode MS', 'Arial';");
        addBtn.setOnAction(e -> showAddAddressDialog(stage));
        
        header.getChildren().addAll(backBtn, title, addBtn);
        return header;
    }
    
    private VBox createContent(Stage stage) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 40, 40, 40));
        
        // Load addresses
        java.util.List<Address> addresses = addressDAO.getAddressesByUserId(currentUserId);
        
        if (addresses.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            
            Label emptyLabel = new Label("📍");
            emptyLabel.setStyle("-fx-font-size: 64px; -fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', 'Arial Unicode MS', 'Arial';");
            
            Label emptyText = new Label("Chưa có địa chỉ nào");
            emptyText.getStyleClass().add("empty-label-modern");
            
            Button addBtn = new Button("➕ Thêm địa chỉ");
            addBtn.getStyleClass().add("add-button-modern");
            addBtn.setOnAction(e -> showAddAddressDialog(stage));
            
            emptyBox.getChildren().addAll(emptyLabel, emptyText, addBtn);
            content.getChildren().add(emptyBox);
        } else {
            for (Address address : addresses) {
                VBox addressCard = createAddressCard(address, stage);
                content.getChildren().add(addressCard);
            }
        }
        
        return content;
    }
    
    private VBox createAddressCard(Address address, Stage stage) {
        VBox card = new VBox(15);
        card.getStyleClass().add("address-card-modern");
        card.setPadding(new Insets(25));
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(address.getFullName());
        nameLabel.getStyleClass().add("address-name-modern");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        if (address.isDefault()) {
            Label defaultBadge = new Label("Mặc định");
            defaultBadge.getStyleClass().add("default-badge-modern");
            header.getChildren().add(defaultBadge);
        }
        
        header.getChildren().add(nameLabel);
        
        Label phoneLabel = new Label("📱 " + address.getPhone());
        phoneLabel.getStyleClass().add("address-detail-modern");
        phoneLabel.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', 'Arial Unicode MS', 'Arial';");
        
        Label addressLabel = new Label("📍 " + address.getFullAddress());
        addressLabel.getStyleClass().add("address-detail-modern");
        addressLabel.setWrapText(true);
        addressLabel.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', 'Arial Unicode MS', 'Arial';");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("✏️ Sửa");
        editBtn.getStyleClass().add("edit-button-modern");
        editBtn.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', 'Arial Unicode MS', 'Arial';");
        editBtn.setOnAction(e -> showEditAddressDialog(address, stage));
        
        Button deleteBtn = new Button("🗑️ Xóa");
        deleteBtn.getStyleClass().add("delete-button-modern");
        deleteBtn.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', 'Arial Unicode MS', 'Arial';");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận");
            confirm.setHeaderText("Bạn có chắc chắn muốn xóa địa chỉ này?");
            confirm.setContentText("Hành động này không thể hoàn tác.");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (addressDAO.deleteAddress(address.getId())) {
                        // Refresh
                        AccountController accountController = new AccountController();
                        accountController.show(stage);
                    }
                }
            });
        });
        
        if (!address.isDefault()) {
            Button setDefaultBtn = new Button("⭐ Đặt mặc định");
            setDefaultBtn.getStyleClass().add("default-button-modern");
            setDefaultBtn.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Segoe UI Symbol', 'Apple Color Emoji', 'Noto Color Emoji', 'Arial Unicode MS', 'Arial';");
            setDefaultBtn.setOnAction(e -> {
                address.setDefault(true);
                if (addressDAO.updateAddress(address)) {
                    // Refresh
                    AccountController accountController = new AccountController();
                    accountController.show(stage);
                }
            });
            buttonBox.getChildren().add(setDefaultBtn);
        }
        
        buttonBox.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(header, phoneLabel, addressLabel, buttonBox);
        
        return card;
    }
    
    private void showAddAddressDialog(Stage parentStage) {
        showAddressDialog(null, parentStage);
    }
    
    private void showEditAddressDialog(Address address, Stage parentStage) {
        showAddressDialog(address, parentStage);
    }
    
    private void showAddressDialog(Address address, Stage parentStage) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
        dialog.initOwner(parentStage);
        dialog.initStyle(javafx.stage.StageStyle.UTILITY);
        dialog.setTitle(address == null ? "Thêm địa chỉ" : "Sửa địa chỉ");
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("address-dialog");
        
        TextField fullNameField = new TextField(address != null ? address.getFullName() : "");
        fullNameField.setPromptText("Họ và tên người nhận");
        fullNameField.getStyleClass().add("text-field-modern");
        
        TextField phoneField = new TextField(address != null ? address.getPhone() : "");
        phoneField.setPromptText("Số điện thoại");
        phoneField.getStyleClass().add("text-field-modern");
        
        TextField provinceField = new TextField(address != null ? address.getProvince() : "");
        provinceField.setPromptText("Tỉnh/Thành phố");
        provinceField.getStyleClass().add("text-field-modern");
        
        TextField districtField = new TextField(address != null ? address.getDistrict() : "");
        districtField.setPromptText("Quận/Huyện");
        districtField.getStyleClass().add("text-field-modern");
        
        TextField wardField = new TextField(address != null ? address.getWard() : "");
        wardField.setPromptText("Phường/Xã");
        wardField.getStyleClass().add("text-field-modern");
        
        TextField streetField = new TextField(address != null ? address.getStreetAddress() : "");
        streetField.setPromptText("Số nhà, tên đường");
        streetField.getStyleClass().add("text-field-modern");
        
        CheckBox defaultCheck = new CheckBox("Đặt làm địa chỉ mặc định");
        defaultCheck.setSelected(address != null && address.isDefault());
        defaultCheck.getStyleClass().add("check-box-modern");
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button saveBtn = new Button("💾 Lưu");
        saveBtn.getStyleClass().add("save-button-modern");
        saveBtn.setOnAction(e -> {
            if (fullNameField.getText().isEmpty() || phoneField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cảnh báo");
                alert.setHeaderText("Vui lòng điền đầy đủ thông tin");
                alert.setContentText("Họ tên và số điện thoại là bắt buộc.");
                alert.showAndWait();
                return;
            }
            
            Address addr = address != null ? address : new Address();
            if (address == null) {
                addr.setUserId(currentUserId);
            }
            addr.setFullName(fullNameField.getText());
            addr.setPhone(phoneField.getText());
            addr.setProvince(provinceField.getText());
            addr.setDistrict(districtField.getText());
            addr.setWard(wardField.getText());
            addr.setStreetAddress(streetField.getText());
            addr.setDefault(defaultCheck.isSelected());
            
            boolean success = address != null ? 
                addressDAO.updateAddress(addr) : 
                addressDAO.addAddress(addr);
            
            if (success) {
                dialog.close();
                // Refresh
                AccountController accountController = new AccountController();
                accountController.show(parentStage);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Không thể lưu địa chỉ");
                alert.setContentText("Vui lòng thử lại sau.");
                alert.showAndWait();
            }
        });
        
        Button cancelBtn = new Button("✕ Hủy");
        cancelBtn.getStyleClass().add("cancel-button-modern");
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        
        root.getChildren().addAll(
            fullNameField,
            phoneField,
            provinceField,
            districtField,
            wardField,
            streetField,
            defaultCheck,
            buttonBox
        );
        
        Scene scene = new Scene(root, 450, 500);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }
    
    private HBox createBottomNavigation(Stage stage) {
        HBox nav = new HBox();
        nav.getStyleClass().add("bottom-nav-modern");
        nav.setPadding(new Insets(15));
        
        String[] navItems = {"Trang chủ", "Danh mục", "Yêu thích", "Tài khoản"};
        String[] navIcons = {"🏠", "📦", "❤️", "👤"};
        
        for (int i = 0; i < navItems.length; i++) {
            VBox navItem = new VBox(5);
            navItem.setAlignment(Pos.CENTER);
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

























