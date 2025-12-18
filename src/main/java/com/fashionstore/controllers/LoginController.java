package com.fashionstore.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.fashionstore.dao.UserDAO;
import com.fashionstore.models.User;
import com.fashionstore.database.DatabaseConnection;
import java.sql.*;

public class LoginController {
    
    public void show(Stage stage) {
        VBox root = new VBox(25);
        root.getStyleClass().add("login-root");
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setPadding(new javafx.geometry.Insets(50));
        root.setPrefWidth(450);
        root.setPrefHeight(600);
        
        // Logo
        Label logo = new Label("👔 FashionStore");
        logo.getStyleClass().add("login-logo");
        
        Label title = new Label("Đăng nhập");
        title.getStyleClass().add("login-title");
        
        // Form
        VBox form = new VBox(20);
        form.setPrefWidth(400);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("login-input");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");
        passwordField.getStyleClass().add("login-input");
        
        CheckBox rememberMe = new CheckBox("Ghi nhớ đăng nhập");
        
        Button loginBtn = new Button("Đăng nhập");
        loginBtn.getStyleClass().add("login-button");
        loginBtn.setPrefWidth(400);
        loginBtn.setPrefHeight(45);
        
        Hyperlink forgotPassword = new Hyperlink("Quên mật khẩu?");
        forgotPassword.getStyleClass().add("login-link");
        
        Separator separator = new Separator();
        
        Label registerLabel = new Label("Chưa có tài khoản?");
        Hyperlink registerLink = new Hyperlink("Đăng ký ngay");
        registerLink.getStyleClass().add("login-link");
        
        HBox registerBox = new HBox(5);
        registerBox.setAlignment(javafx.geometry.Pos.CENTER);
        registerBox.getChildren().addAll(registerLabel, registerLink);
        
        form.getChildren().addAll(emailField, passwordField, rememberMe, loginBtn, forgotPassword, separator, registerBox);
        
        // Login action
        loginBtn.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            
            if (email.isEmpty() || password.isEmpty()) {
                showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            
            if ("admin".equalsIgnoreCase(email) && "123456".equals(password)) {
                AdminController adminController = new AdminController();
                adminController.show(stage);
                return;
            }
            
            User user = authenticate(email, password);
            if (user != null) {
                UserDAO.setCurrentUserId(user.getId());
                HomeController homeController = new HomeController();
                homeController.show(stage);
            } else {
                showAlert("Lỗi", "Email hoặc mật khẩu không đúng!");
            }
        });
        
        // Register action
        registerLink.setOnAction(e -> {
            RegisterController registerController = new RegisterController();
            registerController.show(stage);
        });
        
        root.getChildren().addAll(logo, title, form);
        
        Scene scene = new Scene(root, 450, 600);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        stage.setScene(scene);
        stage.setTitle("FashionStore - Đăng nhập");
        stage.setResizable(false);
        stage.show();
    }
    
    private User authenticate(String email, String password) {
        // First ensure password column exists
        com.fashionstore.database.DatabaseSetup.checkAndCreatePasswordColumn();
        
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, email);
                    pstmt.setString(2, password); // In production, use hashed passwords
                    
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setName(rs.getString("name"));
                        user.setEmail(rs.getString("email"));
                        user.setPhone(rs.getString("phone"));
                        user.setMembershipLevel(rs.getString("membership_level"));
                        user.setPoints(rs.getInt("points"));
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return null;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

