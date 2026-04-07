package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AdminController extends VBox {
    private final ProductService productService;
    private final TableView<Product> productTable;
    private final TextField nameField, priceField, stockField;
    private final TextArea descField;
    private final ComboBox<String> categoryCombo;
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private List<String[]> categories;

    public AdminController() {
        this.productService = new ProductService();
        this.setPadding(new Insets(20));
        this.setSpacing(20);

        Label title = new Label("Admin Panel - Inventory Management");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Form Section
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        nameField = new TextField();
        priceField = new TextField();
        stockField = new TextField();
        descField = new TextArea();
        descField.setPrefRowCount(3);
        categoryCombo = new ComboBox<>();

        form.add(new Label("Product Name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Price ($):"), 0, 1);
        form.add(priceField, 1, 1);
        form.add(new Label("Stock:"), 0, 2);
        form.add(stockField, 1, 2);
        form.add(new Label("Category:"), 0, 3);
        form.add(categoryCombo, 1, 3);
        form.add(new Label("Description:"), 0, 4);
        form.add(descField, 1, 4);

        // Buttons Section
        HBox buttons = new HBox(10);
        Button addButton = new Button("Add Product");
        Button updateButton = new Button("Update Product");
        Button deleteButton = new Button("Delete Product");
        Button clearButton = new Button("Clear Form");

        addButton.setOnAction(e -> handleAdd());
        updateButton.setOnAction(e -> handleUpdate());
        deleteButton.setOnAction(e -> handleDelete());
        clearButton.setOnAction(e -> clearForm());

        buttons.getChildren().addAll(addButton, updateButton, deleteButton, clearButton);

        // Table Section
        productTable = new TableView<>();
        setupTable();

        this.getChildren().addAll(title, form, buttons, productTable);

        loadInitialData();
    }

    private void setupTable() {
        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getProductId()));
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrice()));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategoryName()));

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getStockQuantity()));

        productTable.getColumns().addAll(idCol, nameCol, priceCol, categoryCol, stockCol);
        productTable.setItems(productList);

        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillForm(newSelection);
            }
        });
    }

    private void loadInitialData() {
        try {
            categories = productService.getAllCategories();
            categoryCombo.getItems().clear();
            for (String[] cat : categories) {
                categoryCombo.getItems().add(cat[1]);
            }

            refreshTable();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void refreshTable() {
        try {
            productList.setAll(productService.searchProducts("", null, 1, 100, "Name (A-Z)"));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to fetch products: " + e.getMessage());
        }
    }

    private void fillForm(Product p) {
        nameField.setText(p.getName());
        priceField.setText(String.valueOf(p.getPrice()));
        stockField.setText(String.valueOf(p.getStockQuantity()));
        descField.setText(p.getDescription());
        categoryCombo.setValue(p.getCategoryName());
    }

    private void clearForm() {
        nameField.clear();
        priceField.clear();
        stockField.clear();
        descField.clear();
        categoryCombo.setValue(null);
        productTable.getSelectionModel().clearSelection();
    }

    private void handleAdd() {
        if (!validateInput()) return;

        try {
            Product p = createProductFromForm();
            productService.addProduct(p);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");
            clearForm();
            refreshTable();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Add Error", "Failed to add product: " + e.getMessage());
        }
    }

    private void handleUpdate() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to update.");
            return;
        }

        if (!validateInput()) return;

        try {
            Product p = createProductFromForm();
            p.setProductId(selected.getProductId());
            productService.updateProduct(p);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
            refreshTable();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Update Error", "Failed to update product: " + e.getMessage());
        }
    }

    private void handleDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Product: " + selected.getName());
        confirm.setContentText("Are you sure you want to delete this product? This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productService.deleteProduct(selected.getProductId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully!");
                clearForm();
                refreshTable();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Delete Error", "Failed to delete product: " + e.getMessage());
            }
        }
    }

    private boolean validateInput() {
        if (nameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Product name cannot be empty.");
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceField.getText());
            if (price < 0) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Price cannot be negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Price must be a valid number.");
            return false;
        }

        try {
            int stock = Integer.parseInt(stockField.getText());
            if (stock < 0) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Stock cannot be negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Stock must be a valid integer.");
            return false;
        }

        if (categoryCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a category.");
            return false;
        }

        return true;
    }

    private Product createProductFromForm() {
        Product p = new Product();
        p.setName(nameField.getText());
        p.setDescription(descField.getText());
        p.setPrice(Double.parseDouble(priceField.getText()));
        p.setStockQuantity(Integer.parseInt(stockField.getText()));
        
        String catName = categoryCombo.getValue();
        for (String[] cat : categories) {
            if (cat[1].equals(catName)) {
                p.setCategoryId(Integer.parseInt(cat[0]));
                p.setCategoryName(catName);
                break;
            }
        }
        return p;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
