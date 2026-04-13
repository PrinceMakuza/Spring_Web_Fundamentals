package com.ecommerce;

import com.ecommerce.controller.LoginController;
import com.ecommerce.controller.DashboardController;
import com.ecommerce.dao.DatabaseConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * MainApp is the entry point for the Smart E-Commerce System.
 * Refactored to maintain window state and size across views.
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private Scene mainScene;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Smart E-Commerce System");

        // Initialize with a standard, compact size
        mainScene = new Scene(new StackPane(), 1050, 700);
        applyStyles(mainScene);
        
        showLogin();

        primaryStage.setScene(mainScene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void showLogin() {
        LoginController loginView = new LoginController(this::showDashboard);
        mainScene.setRoot(loginView);
    }

    private void showDashboard() {
        DashboardController dashboard = new DashboardController(this::showLogin);
        mainScene.setRoot(dashboard);
    }

    private void applyStyles(Scene scene) {
        String cssPath = getClass().getResource("/css/styles.css") != null
            ? getClass().getResource("/css/styles.css").toExternalForm()
            : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }
    }

    @Override
    public void stop() {
        DatabaseConnection.closePool();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
