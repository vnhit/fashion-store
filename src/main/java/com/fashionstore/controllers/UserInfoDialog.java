package com.fashionstore.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.fashionstore.dao.UserDAO;
import com.fashionstore.models.User;

public class UserInfoDialog {
    
    public static void show(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parentStage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Thông tin tài khoản");
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("user-info-dialog");
        
        // Get current user
        int currentUserId = UserDAO.getCurrentUserId();
        User user = new UserDAO().getUserById(currentUserId);
        
        // Avatar
        StackPane avatarContainer = new StackPane();
        javafx.scene.shape.Circle avatar = new javafx.scene.shape.Circle(60);
        avatar.getStyleClass().add("avatar-circle-modern");
        avatarContainer.getChildren().add(avatar);
        
        // User info
        VBox infoBox = new VBox(15);
        infoBox.setAlignment(Pos.CENTER);
        
        Label nameLabel = new Label(user.getName());
        nameLabel.getStyleClass().add("user-info-name");
        
        Label emailLabel = new Label("📧 " + (user.getEmail() != null ? user.getEmail() : "Chưa có email"));
        emailLabel.getStyleClass().add("user-info-detail");
        
        Label phoneLabel = new Label("📱 " + (user.getPhone() != null ? user.getPhone() : "Chưa có số điện thoại"));
        phoneLabel.getStyleClass().add("user-info-detail");
        
        Label membershipLabel = new Label("👑 Thành viên: " + user.getMembershipLevel());
        membershipLabel.getStyleClass().add("user-info-detail");
        
        Label pointsLabel = new Label("⭐ Điểm tích lũy: " + user.getPoints() + " điểm");
        pointsLabel.getStyleClass().add("user-info-detail");
        
        infoBox.getChildren().addAll(nameLabel, emailLabel, phoneLabel, membershipLabel, pointsLabel);
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button editBtn = new Button("✏️ Chỉnh sửa");
        editBtn.getStyleClass().add("user-info-button");
        editBtn.setOnAction(e -> {
            dialog.close();
            AccountController accountController = new AccountController();
            accountController.show(parentStage);
        });
        
        Button logoutBtn = new Button("🚪 Đăng xuất");
        logoutBtn.getStyleClass().add("user-info-button-logout");
        logoutBtn.setOnAction(e -> {
            // Xóa thông tin đăng nhập hiện tại và quay lại màn hình đăng nhập
            UserDAO.setCurrentUserId(0);
            dialog.close();
            LoginController loginController = new LoginController();
            loginController.show(parentStage);
        });
        
        Button closeBtn = new Button("✕ Đóng");
        closeBtn.getStyleClass().add("user-info-button-close");
        closeBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(editBtn, logoutBtn, closeBtn);
        
        root.getChildren().addAll(avatarContainer, infoBox, buttonBox);
        
        Scene scene = new Scene(root, 400, 450);
        try {
            scene.getStylesheets().add(UserInfoDialog.class.getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }
}























