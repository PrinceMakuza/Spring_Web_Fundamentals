package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class ProductController extends BorderPane {
    private final ProductService productService;
    private final TableView<Product> productTable;
    private final TextField searchField;
    private final ComboBox<String> categoryFilter;
    private final ComboBox<String> sortCombo;
    private final Label pageLabel;
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    
    private int currentPage = 1;
    private final int pageSize = 10;
    private int totalProducts = 0;
    private List<String[]> categories;

    public ProductController() {
        this.productService = new ProductService();
        this.setPadding(new Insets(20));

        // Top Search & Filter Bar
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        searchField = new TextField();
        searchField.setPromptText("Search products...");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("All Categories");

        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Name (A-Z)", "Price (Low to High)", "Price (High to Low)");
        sortCombo.setValue("Name (A-Z)");

        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> {
            currentPage = 1;
            loadData();
        });

        topBar.getChildren().addAll(new Label("Search:"), searchField, new Label("Category:"), categoryFilter, new Label("Sort:"), sortCombo, searchBtn);

        // Table
        productTable = new TableView<>();
        setupTable();

        // Pagination Bar
        HBox paginationBar = new HBox(20);
        paginationBar.setAlignment(Pos.CENTER);
        paginationBar.setPadding(new Insets(20, 0, 0, 0));

        Button prevBtn = new Button("<< Previous");
        Button nextBtn = new Button("Next >>");
        pageLabel = new Label("Page 1");

        prevBtn.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadData();
            }
        });

        nextBtn.setOnAction(e -> {
            if (currentPage * pageSize < totalProducts) {
                currentPage++;
                loadData();
            }
        });

        paginationBar.getChildren().addAll(prevBtn, pageLabel, nextBtn);

        this.setTop(topBar);
        this.setCenter(productTable);
        this.setBottom(paginationBar);

        loadInitialData();
    }

    private void setupTable() {
        TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(250);

        TableColumn<Product, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        descCol.setPrefWidth(300);

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrice()));
        priceCol.setCellFactory(tc -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });

        TableColumn<Product, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategoryName()));

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getStockQuantity()));

        productTable.getColumns().addAll(nameCol, descCol, priceCol, catCol, stockCol);
        productTable.setItems(productList);
    }

    private void loadInitialData() {
        try {
            categories = productService.getAllCategories();
            categoryFilter.getItems().clear();
            categoryFilter.getItems().add("All Categories");
            for (String[] cat : categories) {
                categoryFilter.getItems().add(cat[1]);
            }
            categoryFilter.setValue("All Categories");

            loadData();
        } catch (SQLException e) {
            showError("Database Error", "Failed to load initial data: " + e.getMessage());
        }
    }

    private void loadData() {
        try {
            String searchTerm = searchField.getText().trim();
            Integer catId = null;
            String selectedCat = categoryFilter.getValue();
            
            if (selectedCat != null && !selectedCat.equals("All Categories")) {
                for (String[] cat : categories) {
                    if (cat[1].equals(selectedCat)) {
                        catId = Integer.parseInt(cat[0]);
                        break;
                    }
                }
            }

            totalProducts = productService.getSearchCount(searchTerm, catId);
            long startTime = System.currentTimeMillis();
            List<Product> products = productService.searchProducts(searchTerm, catId, currentPage, pageSize, sortCombo.getValue());
            long endTime = System.currentTimeMillis();
            
            System.out.println("Search took: " + (endTime - startTime) + "ms");
            
            productList.setAll(products);
            updatePaginationUI();
        } catch (SQLException e) {
            showError("Search Error", "Failed to load products: " + e.getMessage());
        }
    }

    private void updatePaginationUI() {
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
        if (totalPages == 0) totalPages = 1;
        pageLabel.setText(String.format("Page %d of %d", currentPage, totalPages));
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
