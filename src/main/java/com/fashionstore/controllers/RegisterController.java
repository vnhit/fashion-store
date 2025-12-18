package com.fashionstore.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.fashionstore.dao.UserDAO;
import com.fashionstore.database.DatabaseConnection;
import java.sql.*;

public class RegisterController {
    
    public void show(Stage stage) {
        VBox root = new VBox(25);
        root.getStyleClass().add("login-root");
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setPadding(new javafx.geometry.Insets(50));
        root.setPrefWidth(450);
        root.setPrefHeight(700);
        
        // Logo
        Label logo = new Label("👔 FashionStore");
        logo.getStyleClass().add("login-logo");
        
        Label title = new Label("Đăng ký");
        title.getStyleClass().add("login-title");
        
        // Form
        VBox form = new VBox(20);
        form.setPrefWidth(400);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Họ và tên");
        nameField.getStyleClass().add("login-input");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("login-input");
        
        TextField phoneField = new TextField();
        phoneField.setPromptText("Số điện thoại");
        phoneField.getStyleClass().add("login-input");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");
        passwordField.getStyleClass().add("login-input");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Xác nhận mật khẩu");
        confirmPasswordField.getStyleClass().add("login-input");
        
        Button registerBtn = new Button("Đăng ký");
        registerBtn.getStyleClass().add("login-button");
        registerBtn.setPrefWidth(400);
        registerBtn.setPrefHeight(45);
        
        Separator separator = new Separator();
        
        Label loginLabel = new Label("Đã có tài khoản?");
        Hyperlink loginLink = new Hyperlink("Đăng nhập ngay");
        loginLink.getStyleClass().add("login-link");
        
        HBox loginBox = new HBox(5);
        loginBox.setAlignment(javafx.geometry.Pos.CENTER);
        loginBox.getChildren().addAll(loginLabel, loginLink);
        
        form.getChildren().addAll(nameField, emailField, phoneField, passwordField, 
                                  confirmPasswordField, registerBtn, separator, loginBox);
        
        // Register action
        registerBtn.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                showAlert("Lỗi", "Mật khẩu xác nhận không khớp!");
                return;
            }
            
            if (registerUser(name, email, phone, password)) {
                showAlert("Thành công", "Đăng ký thành công! Vui lòng đăng nhập.");
                LoginController loginController = new LoginController();
                loginController.show(stage);
            } else {
                showAlert("Lỗi", "Email đã tồn tại hoặc có lỗi xảy ra!");
            }
        });
        
        // Login action
        loginLink.setOnAction(e -> {
            LoginController loginController = new LoginController();
            loginController.show(stage);
        });
        
        root.getChildren().addAll(logo, title, form);
        
        Scene scene = new Scene(root, 450, 700);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        stage.setScene(scene);
        stage.setTitle("FashionStore - Đăng ký");
        stage.setResizable(false);
        stage.show();
    }
    
    private boolean registerUser(String name, String email, String phone, String password) {
        // First ensure password column exists
        com.fashionstore.database.DatabaseSetup.checkAndCreatePasswordColumn();
        
        String checkSql = "SELECT id FROM users WHERE email = ?";
        String insertSql = "INSERT INTO users (name, email, phone, password, membership_level, points) VALUES (?, ?, ?, ?, 'Silver', 0)";
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Check if email exists
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, email);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        return false; // Email already exists
                    }
                }
                
                // Insert new user
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, email);
                    insertStmt.setString(3, phone);
                    insertStmt.setString(4, password); // In production, hash the password
                    return insertStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return false;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

