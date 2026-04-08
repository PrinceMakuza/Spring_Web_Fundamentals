package com.ecommerce.controller;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;
import com.ecommerce.dao.CategoryDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * AdminController provides the admin panel for product and category management.
 * Implements User Story 2.1: Add/update/delete products and categories via JavaFX,
 * with input validation, feedback messages, and duplicate prevention.
 *
 * Layout uses a toggle between Product Management and Category Management panels.
 * All data operations use parameterized queries via the service and DAO layers.
 */
public class AdminController extends VBox {
    private final ProductService productService;
    private final CategoryDAO categoryDAO;

    // Product management controls (direct references, no lookup needed)
    private final TableView<Product> productTable = new TableView<>();
    private final TextField nameField = new TextField();
    private final TextField priceField = new TextField();
    private final TextField stockField = new TextField();
    private final TextArea descField = new TextArea();
    private final ComboBox<String> categoryCombo = new ComboBox<>();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    // Category management controls
    private final TableView<Category> categoryTable = new TableView<>();
    private final TextField catNameField = new TextField();
    private final TextArea catDescField = new TextArea();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();

    private List<Category> categories;

    // Callback to refresh product browser when admin changes data
    private Runnable onDataChanged;

    public AdminController() {
        this.productService = new ProductService();
        this.categoryDAO = new CategoryDAO();
        this.setSpacing(20);
        this.setPadding(new Insets(0));
        this.getStyleClass().add("main-content");

        // Title
        VBox header = new VBox(5);
        Label title = new Label("⚙  Admin Panel");
        title.getStyleClass().add("content-title");
        Label subtitle = new Label("Manage products and categories");
        subtitle.getStyleClass().add("content-subtitle");
        header.getChildren().addAll(title, subtitle);

        // Internal tab-like toggle for Product vs Category management
        HBox toggleBar = new HBox(10);
        toggleBar.setAlignment(Pos.CENTER_LEFT);

        Button productTab = new Button("📦  Product Management");
        productTab.getStyleClass().addAll("button-primary");
        Button categoryTab = new Button("📂  Category Management");

        StackPane contentPane = new StackPane();

        // Build both panels using direct field references
        VBox productPanel = buildProductPanel();
        VBox categoryPanel = buildCategoryPanel();
        categoryPanel.setVisible(false);

        contentPane.getChildren().addAll(productPanel, categoryPanel);
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        productTab.setOnAction(e -> {
            productPanel.setVisible(true);
            categoryPanel.setVisible(false);
            productTab.getStyleClass().removeAll("button");
            if (!productTab.getStyleClass().contains("button-primary")) {
                productTab.getStyleClass().add("button-primary");
            }
            categoryTab.getStyleClass().removeAll("button-primary");
        });

        categoryTab.setOnAction(e -> {
            productPanel.setVisible(false);
            categoryPanel.setVisible(true);
            categoryTab.getStyleClass().removeAll("button");
            if (!categoryTab.getStyleClass().contains("button-primary")) {
                categoryTab.getStyleClass().add("button-primary");
            }
            productTab.getStyleClass().removeAll("button-primary");
        });

        toggleBar.getChildren().addAll(productTab, categoryTab);

        this.getChildren().addAll(header, toggleBar, contentPane);

        loadInitialData();
    }

    /**
     * Sets a callback to be invoked when admin changes data.
     * Used to refresh the product browser view.
     */
    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }

    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    // ==================== Product Panel ====================

    /**
     * Builds the Product Management panel with form, buttons, and table.
     * Uses direct field references (nameField, priceField, etc.) declared at class level.
     */
    private VBox buildProductPanel() {
        VBox panel = new VBox(15);

        // Product Form (Card)
        VBox formCard = new VBox(12);
        formCard.getStyleClass().add("card");

        Label formTitle = new Label("Product Form");
        formTitle.getStyleClass().add("label-bright");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(10);

        nameField.setPromptText("Product name");
        nameField.setPrefWidth(300);
        priceField.setPromptText("0.00");
        stockField.setPromptText("0");
        categoryCombo.setPromptText("Select category");
        categoryCombo.setPrefWidth(200);
        descField.setPrefRowCount(2);
        descField.setPromptText("Product description...");

        addFormRow(form, 0, "Product Name:", nameField);
        addFormRow(form, 1, "Price ($):", priceField);
        addFormRow(form, 2, "Stock Qty:", stockField);
        addFormRow(form, 3, "Category:", categoryCombo);
        addFormRow(form, 4, "Description:", descField);

        // Action Buttons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(5, 0, 0, 0));

        Button addBtn = new Button("➕  Add Product");
        addBtn.getStyleClass().add("button-success");
        addBtn.setOnAction(e -> handleAddProduct());

        Button updateBtn = new Button("✏  Update");
        updateBtn.getStyleClass().add("button-primary");
        updateBtn.setOnAction(e -> handleUpdateProduct());

        Button deleteBtn = new Button("🗑  Delete");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> handleDeleteProduct());

        Button clearBtn = new Button("Clear Form");
        clearBtn.setOnAction(e -> clearProductForm());

        buttons.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);

        formCard.getChildren().addAll(formTitle, form, buttons);

        // Product Table
        productTable.setMinHeight(300);
        VBox.setVgrow(productTable, Priority.ALWAYS);
        setupProductTable();

        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillProductForm(newVal);
            }
        });

        panel.getChildren().addAll(formCard, productTable);
        return panel;
    }

    /**
     * Sets up product table columns: ID, Name, Price, Category, Stock.
     */
    private void setupProductTable() {
        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getProductId()));
        idCol.setPrefWidth(60);

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Product, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data ->
            new SimpleStringProperty(String.format("$%.2f", data.getValue().getPrice()))
        );
        priceCol.setPrefWidth(100);

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));
        categoryCol.setPrefWidth(130);

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStockQuantity()));
        stockCol.setPrefWidth(80);

        productTable.getColumns().addAll(idCol, nameCol, priceCol, categoryCol, stockCol);
        productTable.setItems(productList);
    }

    /**
     * Fills the product form fields when a row is selected in the table.
     */
    private void fillProductForm(Product p) {
        nameField.setText(p.getName());
        priceField.setText(String.valueOf(p.getPrice()));
        stockField.setText(String.valueOf(p.getStockQuantity()));
        descField.setText(p.getDescription());
        categoryCombo.setValue(p.getCategoryName());
    }

    /**
     * Clears all product form fields and deselects the table row.
     */
    private void clearProductForm() {
        nameField.clear();
        priceField.clear();
        stockField.clear();
        descField.clear();
        categoryCombo.setValue(null);
        productTable.getSelectionModel().clearSelection();
    }

    /**
     * Handles adding a new product.
     * Validates input, creates product via service layer, shows feedback dialog.
     * Prevents duplicate names via database UNIQUE constraint.
     */
    private void handleAddProduct() {
        if (!validateProductInput()) return;
        try {
            Product p = buildProductFromForm();
            productService.addProduct(p);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product '" + p.getName() + "' added successfully!");
            clearProductForm();
            refreshProducts();
            notifyDataChanged();
        } catch (SQLException e) {
            if (e.getMessage() != null && (e.getMessage().contains("duplicate") || e.getMessage().contains("unique"))) {
                showAlert(Alert.AlertType.ERROR, "Duplicate Error", "A product with this name already exists.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Add Error", "Failed to add product: " + e.getMessage());
            }
        }
    }

    /**
     * Handles updating the selected product.
     * Requires a table row to be selected first.
     */
    private void handleUpdateProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to update.");
            return;
        }
        if (!validateProductInput()) return;
        try {
            Product p = buildProductFromForm();
            p.setProductId(selected.getProductId());
            productService.updateProduct(p);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
            refreshProducts();
            notifyDataChanged();
        } catch (SQLException e) {
            if (e.getMessage() != null && (e.getMessage().contains("duplicate") || e.getMessage().contains("unique"))) {
                showAlert(Alert.AlertType.ERROR, "Duplicate Error", "A product with this name already exists.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Error", "Failed to update product: " + e.getMessage());
            }
        }
    }

    /**
     * Handles deleting the selected product with a confirmation dialog.
     * Shows error if product has order references (ON DELETE RESTRICT).
     */
    private void handleDeleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Product: " + selected.getName());
        confirm.setContentText("Are you sure? This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productService.deleteProduct(selected.getProductId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully!");
                clearProductForm();
                refreshProducts();
                notifyDataChanged();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Delete Error",
                    "Cannot delete product (may have orders referencing it): " + e.getMessage());
            }
        }
    }

    /**
     * Validates all product form inputs.
     * Checks: non-empty name, valid price >= 0, valid stock >= 0, category selected.
     */
    private boolean validateProductInput() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Product name cannot be empty.");
            return false;
        }
        try {
            double price = Double.parseDouble(priceField.getText());
            if (price < 0) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Price must be ≥ 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Price must be a valid number.");
            return false;
        }
        try {
            int stock = Integer.parseInt(stockField.getText());
            if (stock < 0) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Stock must be ≥ 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Stock must be a valid integer.");
            return false;
        }
        if (categoryCombo.getValue() == null || categoryCombo.getValue().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a category.");
            return false;
        }
        return true;
    }

    /**
     * Builds a Product object from the form field values.
     * Resolves category name to category ID using the loaded categories list.
     */
    private Product buildProductFromForm() {
        Product p = new Product();
        p.setName(nameField.getText().trim());
        p.setDescription(descField.getText().trim());
        p.setPrice(Double.parseDouble(priceField.getText()));
        p.setStockQuantity(Integer.parseInt(stockField.getText()));
        String catName = categoryCombo.getValue();
        if (categories != null) {
            for (Category cat : categories) {
                if (cat.getName().equals(catName)) {
                    p.setCategoryId(cat.getCategoryId());
                    p.setCategoryName(catName);
                    break;
                }
            }
        }
        return p;
    }

    // ==================== Category Panel ====================

    /**
     * Builds the Category Management panel with form, buttons, and table.
     * Allows admin to add, update, and delete categories.
     */
    private VBox buildCategoryPanel() {
        VBox panel = new VBox(15);

        // Category Form (Card)
        VBox formCard = new VBox(12);
        formCard.getStyleClass().add("card");

        Label formTitle = new Label("Category Form");
        formTitle.getStyleClass().add("label-bright");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(10);

        catNameField.setPromptText("Category name");
        catNameField.setPrefWidth(300);
        catDescField.setPrefRowCount(2);
        catDescField.setPromptText("Category description...");

        addFormRow(form, 0, "Category Name:", catNameField);
        addFormRow(form, 1, "Description:", catDescField);

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("➕  Add Category");
        addBtn.getStyleClass().add("button-success");
        addBtn.setOnAction(e -> handleAddCategory());

        Button updateBtn = new Button("✏  Update");
        updateBtn.getStyleClass().add("button-primary");
        updateBtn.setOnAction(e -> handleUpdateCategory());

        Button deleteBtn = new Button("🗑  Delete");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> handleDeleteCategory());

        Button clearBtn = new Button("Clear Form");
        clearBtn.setOnAction(e -> clearCategoryForm());

        buttons.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);

        formCard.getChildren().addAll(formTitle, form, buttons);

        // Category Table
        categoryTable.setMinHeight(250);
        VBox.setVgrow(categoryTable, Priority.ALWAYS);
        setupCategoryTable();

        categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                catNameField.setText(newVal.getName());
                catDescField.setText(newVal.getDescription() != null ? newVal.getDescription() : "");
            }
        });

        panel.getChildren().addAll(formCard, categoryTable);
        return panel;
    }

    /**
     * Sets up category table columns: ID, Name, Description.
     */
    private void setupCategoryTable() {
        TableColumn<Category, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCategoryId()));
        idCol.setPrefWidth(60);

        TableColumn<Category, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Category, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getDescription() != null ? data.getValue().getDescription() : ""
        ));
        descCol.setPrefWidth(400);

        categoryTable.getColumns().addAll(idCol, nameCol, descCol);
        categoryTable.setItems(categoryList);
    }

    private void clearCategoryForm() {
        catNameField.clear();
        catDescField.clear();
        categoryTable.getSelectionModel().clearSelection();
    }

    /**
     * Handles adding a new category.
     * Prevents duplicate names via UNIQUE constraint.
     */
    private void handleAddCategory() {
        if (catNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Category name cannot be empty.");
            return;
        }
        try {
            Category cat = new Category();
            cat.setName(catNameField.getText().trim());
            cat.setDescription(catDescField.getText().trim());
            categoryDAO.addCategory(cat);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category '" + cat.getName() + "' added!");
            clearCategoryForm();
            loadInitialData();
            notifyDataChanged();
        } catch (SQLException e) {
            if (e.getMessage() != null && (e.getMessage().contains("duplicate") || e.getMessage().contains("unique"))) {
                showAlert(Alert.AlertType.ERROR, "Duplicate Error", "A category with this name already exists.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Add Error", "Failed to add category: " + e.getMessage());
            }
        }
    }

    /**
     * Handles updating the selected category.
     */
    private void handleUpdateCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a category to update.");
            return;
        }
        if (catNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Category name cannot be empty.");
            return;
        }
        try {
            selected.setName(catNameField.getText().trim());
            selected.setDescription(catDescField.getText().trim());
            categoryDAO.updateCategory(selected);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated!");
            loadInitialData();
            notifyDataChanged();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Update Error", "Failed to update: " + e.getMessage());
        }
    }

    /**
     * Handles deleting the selected category with confirmation.
     * Shows error if products reference this category (ON DELETE RESTRICT).
     */
    private void handleDeleteCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a category to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Category: " + selected.getName());
        confirm.setContentText("Note: Categories with products assigned cannot be deleted.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                categoryDAO.deleteCategory(selected.getCategoryId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category deleted!");
                clearCategoryForm();
                loadInitialData();
                notifyDataChanged();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Delete Error",
                    "Cannot delete category (products may depend on it): " + e.getMessage());
            }
        }
    }

    // ==================== Data Loading ====================

    /**
     * Loads categories from the database and refreshes both the category combo
     * (in product form) and the category table.
     */
    private void loadInitialData() {
        try {
            categories = categoryDAO.getAllCategories();

            // Update product form category combo
            categoryCombo.getItems().clear();
            for (Category cat : categories) {
                categoryCombo.getItems().add(cat.getName());
            }

            // Update category table
            categoryList.setAll(categories);

            refreshProducts();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load data: " + e.getMessage());
        }
    }

    /**
     * Refreshes the product table with all products (up to 100).
     */
    private void refreshProducts() {
        try {
            productList.setAll(productService.searchProducts("", null, 1, 100, "Name (A-Z)"));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to refresh products: " + e.getMessage());
        }
    }

    // ==================== Utility ====================

    /**
     * Adds a label + field row to a GridPane form.
     */
    private void addFormRow(GridPane form, int row, String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        form.add(label, 0, row);
        form.add(field, 1, row);
    }

    /**
     * Displays a styled alert dialog with the given type, title, and message.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
