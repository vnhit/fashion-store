package com.fashionstore;

import javafx.application.Application;
import javafx.stage.Stage;
import com.fashionstore.controllers.LoginController;
import com.fashionstore.database.DatabaseSetup;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Check and setup database schema
        DatabaseSetup.checkAndCreatePasswordColumn();
        
        // Show login page first
        LoginController loginController = new LoginController();
        loginController.show(primaryStage);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

