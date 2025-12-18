package com.fashionstore.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.fashionstore.dao.UserDAO;
import com.fashionstore.dao.AddressDAO;
import com.fashionstore.models.User;
import com.fashionstore.models.Address;
import java.io.File;
import java.time.LocalDate;

public class EditUserDialog {
    private UserDAO userDAO = new UserDAO();
    private AddressDAO addressDAO = new AddressDAO();
    private int currentUserId;
    private Runnable onSaveCallback;
    
    public EditUserDialog(Runnable onSaveCallback) {
        this.onSaveCallback = onSaveCallback;
        this.currentUserId = UserDAO.getCurrentUserId();
    }
    
    public void show(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parentStage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Chỉnh sửa thông tin");
        
        ScrollPane scrollPane = new ScrollPane();
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("edit-user-dialog");
        
        // Get current user
        User currentUser = userDAO.getUserById(currentUserId);
        if (currentUser == null) {
            currentUser = new User();
            currentUser.setId(currentUserId);
        }
        final User user = currentUser;
        
        // Avatar section
        VBox avatarSection = new VBox(10);
        avatarSection.setAlignment(Pos.CENTER);
        Label avatarLabel = new Label("Ảnh đại diện");
        avatarLabel.getStyleClass().add("section-title-modern");
        
        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefWidth(120);
        avatarContainer.setPrefHeight(120);
        avatarContainer.setAlignment(Pos.CENTER);
        
        // Placeholder circle background
        javafx.scene.shape.Circle avatarBg = new javafx.scene.shape.Circle(60);
        avatarBg.getStyleClass().add("avatar-circle-modern");
        avatarContainer.getChildren().add(avatarBg);
        
        ImageView avatarImageView = new ImageView();
        avatarImageView.setFitWidth(120);
        avatarImageView.setFitHeight(120);
        avatarImageView.setPreserveRatio(false);
        avatarImageView.setSmooth(true);
        avatarImageView.setCache(true);
        
        // Load current avatar if exists
        String currentAvatarPath = user.getAvatarPath();
        if (currentAvatarPath != null && !currentAvatarPath.isEmpty()) {
            try {
                File avatarFile = new File(currentAvatarPath);
                if (avatarFile.exists()) {
                    Image avatarImage = new Image(avatarFile.toURI().toString(), 120, 120, false, true);
                    avatarImageView.setImage(avatarImage);
                }
            } catch (Exception e) {
                System.err.println("Error loading avatar: " + e.getMessage());
            }
        }
        
        // Create circular clip for avatar - center it properly
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(60, 60, 60);
        avatarImageView.setClip(clip);
        
        StackPane.setAlignment(avatarImageView, Pos.CENTER);
        avatarContainer.getChildren().add(avatarImageView);
        
        Button changeAvatarBtn = new Button("Chọn ảnh");
        changeAvatarBtn.getStyleClass().add("change-avatar-button-modern");
        final String[] selectedAvatarPath = {currentAvatarPath};
        
        changeAvatarBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn ảnh đại diện");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog);
            if (selectedFile != null) {
                try {
                    Image newImage = new Image(selectedFile.toURI().toString(), 120, 120, false, true);
                    avatarImageView.setImage(newImage);
                    selectedAvatarPath[0] = selectedFile.getAbsolutePath();
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi");
                    alert.setHeaderText("Không thể tải ảnh");
                    alert.setContentText("Vui lòng chọn file ảnh hợp lệ.");
                    alert.showAndWait();
                }
            }
        });
        
        avatarSection.getChildren().addAll(avatarLabel, avatarContainer, changeAvatarBtn);
        
        // Thông tin cá nhân
        Label personalInfoLabel = new Label("Thông tin cá nhân");
        personalInfoLabel.getStyleClass().add("section-title-modern");
        
        TextField nameField = new TextField(user.getName());
        nameField.setPromptText("Họ và tên");
        nameField.getStyleClass().add("text-field-modern");
        
        TextField emailField = new TextField(user.getEmail());
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("text-field-modern");
        
        TextField phoneField = new TextField(user.getPhone());
        phoneField.setPromptText("Số điện thoại");
        phoneField.getStyleClass().add("text-field-modern");
        
        DatePicker dateOfBirthPicker = new DatePicker();
        if (user.getDateOfBirth() != null && !user.getDateOfBirth().isEmpty()) {
            try {
                dateOfBirthPicker.setValue(LocalDate.parse(user.getDateOfBirth()));
            } catch (Exception e) {
                // Ignore parse errors
            }
        }
        dateOfBirthPicker.setPromptText("Ngày sinh");
        dateOfBirthPicker.getStyleClass().add("date-picker-modern");
        
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Nam", "Nữ", "Khác");
        if (user.getGender() != null) {
            genderCombo.setValue(user.getGender());
        }
        genderCombo.setPromptText("Giới tính");
        genderCombo.getStyleClass().add("combo-box-modern");
        
        // Địa chỉ giao hàng
        Label addressLabel = new Label("Địa chỉ giao hàng");
        addressLabel.getStyleClass().add("section-title-modern");
        
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Họ và tên người nhận");
        fullNameField.getStyleClass().add("text-field-modern");
        
        TextField addressPhoneField = new TextField();
        addressPhoneField.setPromptText("Số điện thoại");
        addressPhoneField.getStyleClass().add("text-field-modern");
        
        TextField provinceField = new TextField();
        provinceField.setPromptText("Tỉnh/Thành phố");
        provinceField.getStyleClass().add("text-field-modern");
        
        TextField districtField = new TextField();
        districtField.setPromptText("Quận/Huyện");
        districtField.getStyleClass().add("text-field-modern");
        
        TextField wardField = new TextField();
        wardField.setPromptText("Phường/Xã");
        wardField.getStyleClass().add("text-field-modern");
        
        TextField streetField = new TextField();
        streetField.setPromptText("Số nhà, tên đường");
        streetField.getStyleClass().add("text-field-modern");
        
        CheckBox defaultAddressCheck = new CheckBox("Đặt làm địa chỉ mặc định");
        defaultAddressCheck.getStyleClass().add("check-box-modern");
        
        // Load địa chỉ mặc định nếu có
        Address defaultAddress = addressDAO.getAddressesByUserId(currentUserId).stream()
            .filter(Address::isDefault)
            .findFirst()
            .orElse(null);
        
        if (defaultAddress != null) {
            fullNameField.setText(defaultAddress.getFullName());
            addressPhoneField.setText(defaultAddress.getPhone());
            provinceField.setText(defaultAddress.getProvince());
            districtField.setText(defaultAddress.getDistrict());
            wardField.setText(defaultAddress.getWard());
            streetField.setText(defaultAddress.getStreetAddress());
            defaultAddressCheck.setSelected(true);
        }
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button saveBtn = new Button("Lưu");
        saveBtn.getStyleClass().add("save-button-modern");
        saveBtn.setOnAction(e -> {
            // Lưu thông tin user
            user.setName(nameField.getText());
            user.setEmail(emailField.getText());
            user.setPhone(phoneField.getText());
            if (dateOfBirthPicker.getValue() != null) {
                user.setDateOfBirth(dateOfBirthPicker.getValue().toString());
            }
            if (genderCombo.getValue() != null) {
                user.setGender(genderCombo.getValue());
            }
            // Lưu đường dẫn ảnh đại diện
            if (selectedAvatarPath[0] != null && !selectedAvatarPath[0].isEmpty()) {
                user.setAvatarPath(selectedAvatarPath[0]);
            }
            
            if (userDAO.updateUser(user)) {
                // Lưu địa chỉ
                if (!fullNameField.getText().isEmpty() && !addressPhoneField.getText().isEmpty()) {
                    Address address = new Address();
                    if (defaultAddress != null) {
                        address.setId(defaultAddress.getId());
                    }
                    address.setUserId(currentUserId);
                    address.setFullName(fullNameField.getText());
                    address.setPhone(addressPhoneField.getText());
                    address.setProvince(provinceField.getText());
                    address.setDistrict(districtField.getText());
                    address.setWard(wardField.getText());
                    address.setStreetAddress(streetField.getText());
                    address.setDefault(defaultAddressCheck.isSelected());
                    
                    if (defaultAddress != null) {
                        addressDAO.updateAddress(address);
                    } else {
                        addressDAO.addAddress(address);
                    }
                }
                
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
                dialog.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Không thể lưu thông tin");
                alert.setContentText("Vui lòng thử lại sau.");
                alert.showAndWait();
            }
        });
        
        Button cancelBtn = new Button("Hủy");
        cancelBtn.getStyleClass().add("cancel-button-modern");
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        
        root.getChildren().addAll(
            avatarSection,
            personalInfoLabel,
            nameField,
            emailField,
            phoneField,
            dateOfBirthPicker,
            genderCombo,
            addressLabel,
            fullNameField,
            addressPhoneField,
            provinceField,
            districtField,
            wardField,
            streetField,
            defaultAddressCheck,
            buttonBox
        );
        
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent;");
        
        Scene scene = new Scene(scrollPane, 500, 850);
        try {
            scene.getStylesheets().add(EditUserDialog.class.getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }
}

