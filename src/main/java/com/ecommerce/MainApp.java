package com.ecommerce;

import com.ecommerce.controller.AdminController;
import com.ecommerce.controller.ProductController;
import com.ecommerce.dao.DatabaseConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smart E-Commerce System");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab browseTab = new Tab("Browse Products", new ProductController());
        Tab adminTab = new Tab("Admin Panel", new AdminController());

        tabPane.getTabs().addAll(browseTab, adminTab);

        StackPane root = new StackPane(tabPane);
        Scene scene = new Scene(root, 1000, 700);
        
        // Add some basic styling
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Shut down the connection pool when application exits
        DatabaseConnection.closePool();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
