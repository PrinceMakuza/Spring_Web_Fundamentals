package com.ecommerce.controller;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.model.Order;
import com.ecommerce.model.Review;
import com.ecommerce.service.ProductService;
import com.ecommerce.dao.CategoryDAO;
import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.ReviewDAO;
import com.ecommerce.dao.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Enhanced AdminController provides comprehensive management for:
 * Products, Categories, Users (CRUD), Orders, Reviews, and Inventory.
 */
public class AdminController extends VBox {
    private final ProductService productService;
    private final CategoryDAO categoryDAO;
    private final UserDAO userDAO;
    private final OrderDAO orderDAO;
    private final ReviewDAO reviewDAO;

    // Observable Lists
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private final ObservableList<Order> orderList = FXCollections.observableArrayList();
    private final ObservableList<Review> reviewList = FXCollections.observableArrayList();

    // Fields for Product/Category Forms (keep for quick inline add)
    private List<Category> categories;

    public AdminController() {
        this.productService = new ProductService();
        this.categoryDAO = new CategoryDAO();
        this.userDAO = new UserDAO();
        this.orderDAO = new OrderDAO();
        this.reviewDAO = new ReviewDAO();
        
        this.setSpacing(20);
        this.getStyleClass().add("main-content");

        // Header
        VBox header = new VBox(5);
        Label title = new Label("⚙  System Administration");
        title.getStyleClass().addAll("content-title", "label-bright");
        Label subtitle = new Label("Full control over products, customers, and system monitoring");
        subtitle.getStyleClass().add("label-bright");
        header.getChildren().addAll(title, subtitle);

        // Sidebar-like Tab Bar
        HBox toggleBar = new HBox(10);
        toggleBar.setAlignment(Pos.CENTER_LEFT);
        toggleBar.setPadding(new Insets(0, 0, 10, 0));

        Button prodBtn = createTabBtn("📦 Products", true);
        Button catBtn = createTabBtn("📂 Categories", false);
        Button userBtn = createTabBtn("👥 Users", false);
        Button orderBtn = createTabBtn("📜 Orders", false);
        Button invBtn = createTabBtn("🏭 Inventory", false);
        Button reviewBtn = createTabBtn("⭐ Reviews", false);

        StackPane contentPane = new StackPane();
        VBox prodPanel = buildProductPanel();
        VBox catPanel = buildCategoryPanel();
        VBox userPanel = buildUserPanel();
        VBox orderPanel = buildOrderPanel();
        VBox reviewPanel = buildReviewPanel();
        VBox invPanel = buildInventoryPanel();
        
        hideAll(catPanel, userPanel, orderPanel, reviewPanel, invPanel);

        contentPane.getChildren().addAll(prodPanel, catPanel, userPanel, orderPanel, reviewPanel, invPanel);
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        prodBtn.setOnAction(e -> switchTab(prodPanel, prodBtn, contentPane, toggleBar));
        catBtn.setOnAction(e -> switchTab(catPanel, catBtn, contentPane, toggleBar));
        userBtn.setOnAction(e -> { loadUsers(); switchTab(userPanel, userBtn, contentPane, toggleBar); });
        orderBtn.setOnAction(e -> { loadOrders(); switchTab(orderPanel, orderBtn, contentPane, toggleBar); });
        invBtn.setOnAction(e -> { loadInventory(); switchTab(invPanel, invBtn, contentPane, toggleBar); });
        reviewBtn.setOnAction(e -> { loadReviews(); switchTab(reviewPanel, reviewBtn, contentPane, toggleBar); });

        toggleBar.getChildren().addAll(prodBtn, catBtn, userBtn, orderBtn, invBtn, reviewBtn);
        this.getChildren().addAll(header, toggleBar, contentPane);
        loadInitialData();
    }

    private Button createTabBtn(String text, boolean active) {
        Button b = new Button(text);
        if (active) b.getStyleClass().add("button-primary");
        return b;
    }

    private void switchTab(VBox panel, Button btn, StackPane container, HBox bar) {
        container.getChildren().forEach(c -> c.setVisible(false));
        panel.setVisible(true);
        bar.getChildren().forEach(b -> b.getStyleClass().remove("button-primary"));
        btn.getStyleClass().add("button-primary");
    }

    private void hideAll(VBox... panels) {
        for (VBox p : panels) p.setVisible(false);
    }

    // --- PANEL BUILDERS ---

    private VBox buildProductPanel() {
        VBox panel = new VBox(15);
        HBox header = new HBox(10);
        Button add = new Button("➕ Add New Product"); add.getStyleClass().add("button-success");
        Button edit = new Button("✏ Edit Selected"); edit.getStyleClass().add("button-primary");
        Button del = new Button("🗑 Delete Selected"); del.getStyleClass().add("button-danger");
        Button seed = new Button("🌱 Seed Samples"); seed.getStyleClass().add("button-warning");
        header.getChildren().addAll(add, edit, del, seed);

        TableView<Product> table = createProductTable();
        add.setOnAction(e -> handleProductDialog(null));
        edit.setOnAction(e -> { Product s = table.getSelectionModel().getSelectedItem(); if (s != null) handleProductDialog(s); });
        del.setOnAction(e -> { Product s = table.getSelectionModel().getSelectedItem(); if (s != null) handleDeleteProduct(s); });
        seed.setOnAction(e -> handleSeedData());

        panel.getChildren().addAll(header, table);
        return panel;
    }

    private VBox buildCategoryPanel() {
        VBox panel = new VBox(15);
        HBox header = new HBox(10);
        Button add = new Button("➕ Add Category"); add.getStyleClass().add("button-success");
        Button edit = new Button("✏ Edit Selected"); edit.getStyleClass().add("button-primary");
        Button del = new Button("🗑 Delete Selected"); del.getStyleClass().add("button-danger");
        header.getChildren().addAll(add, edit, del);

        TableView<Category> table = createCategoryTable();
        add.setOnAction(e -> handleCategoryDialog(null));
        edit.setOnAction(e -> { Category s = table.getSelectionModel().getSelectedItem(); if (s != null) handleCategoryDialog(s); });
        del.setOnAction(e -> { Category s = table.getSelectionModel().getSelectedItem(); if (s != null) handleDeleteCategory(s); });

        panel.getChildren().addAll(header, table);
        return panel;
    }

    private VBox buildUserPanel() {
        VBox panel = new VBox(15);
        HBox header = new HBox(10);
        Button add = new Button("➕ Add New User"); add.getStyleClass().add("button-success"); add.setOnAction(e -> handleUserDialog(null));
        Button edit = new Button("✏ Edit Selected"); edit.getStyleClass().add("button-primary");
        Button del = new Button("🗑 Delete Selected"); del.getStyleClass().add("button-danger");
        header.getChildren().addAll(add, edit, del);

        TableView<User> table = createUserTable();
        edit.setOnAction(e -> { User s = table.getSelectionModel().getSelectedItem(); if (s != null) handleUserDialog(s); });
        del.setOnAction(e -> { User s = table.getSelectionModel().getSelectedItem(); if (s != null) handleDeleteUser(s); });

        panel.getChildren().addAll(header, table);
        return panel;
    }

    private VBox buildOrderPanel() {
        VBox panel = new VBox(15);
        Label lbl = new Label("System Orders History"); lbl.getStyleClass().add("label-bright");
        TableView<Order> table = createOrderTable();
        panel.getChildren().addAll(lbl, table);
        return panel;
    }

    private VBox buildReviewPanel() {
        VBox panel = new VBox(15);
        Label lbl = new Label("Product Reviews Moderation"); lbl.getStyleClass().add("label-bright");
        TableView<Review> table = createReviewTable();
        panel.getChildren().addAll(lbl, table);
        return panel;
    }

    private VBox buildInventoryPanel() {
        VBox panel = new VBox(15);
        HBox header = new HBox(10);
        Button edit = new Button("🔧 Edit Stock Level"); edit.getStyleClass().add("button-primary");
        header.getChildren().add(edit);

        TableView<Product> table = createInventoryTable();
        edit.setOnAction(e -> { Product s = table.getSelectionModel().getSelectedItem(); if (s != null) handleInventoryDialog(s); });

        panel.getChildren().addAll(header, table);
        return panel;
    }

    // --- TABLES ---

    private TableView<Product> createProductTable() {
        TableView<Product> t = new TableView<>(productList);
        addColumn(t, "ID", 60, d -> new SimpleObjectProperty<Object>(d.getProductId()));
        addColumn(t, "Name", 200, d -> new SimpleStringProperty(d.getName()));
        addColumn(t, "Price", 100, d -> new SimpleStringProperty(String.format("$%.2f", d.getPrice())));
        addColumn(t, "Category", 150, d -> new SimpleStringProperty(d.getCategoryName()));
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return t;
    }

    private TableView<Category> createCategoryTable() {
        TableView<Category> t = new TableView<>(categoryList);
        addColumn(t, "ID", 60, d -> new SimpleObjectProperty<Object>(d.getCategoryId()));
        addColumn(t, "Name", 300, d -> new SimpleStringProperty(d.getName()));
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return t;
    }

    private TableView<User> createUserTable() {
        TableView<User> t = new TableView<>(userList);
        addColumn(t, "ID", 60, d -> new SimpleObjectProperty<Object>(d.getUserId()));
        addColumn(t, "Name", 200, d -> new SimpleStringProperty(d.getName()));
        addColumn(t, "Email", 250, d -> new SimpleStringProperty(d.getEmail()));
        addColumn(t, "Role", 100, d -> new SimpleStringProperty(d.getRole()));
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return t;
    }

    private TableView<Order> createOrderTable() {
        TableView<Order> t = new TableView<>(orderList);
        addColumn(t, "Order ID", 80, d -> new SimpleObjectProperty<Object>(d.getOrderId()));
        addColumn(t, "Customer", 200, d -> new SimpleStringProperty(d.getUserName()));
        addColumn(t, "Total", 100, d -> new SimpleStringProperty(String.format("$%.2f", d.getTotalAmount())));
        addColumn(t, "Date", 200, d -> new SimpleStringProperty(d.getOrderDate().toString().split("T")[0]));
        addColumn(t, "Status", 100, d -> new SimpleStringProperty(d.getStatus()));
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return t;
    }

    private TableView<Review> createReviewTable() {
        TableView<Review> t = new TableView<>(reviewList);
        addColumn(t, "Product", 200, d -> new SimpleStringProperty(d.getProductName()));
        addColumn(t, "User", 150, d -> new SimpleStringProperty(d.getUserName()));
        addColumn(t, "Rating", 80, d -> new SimpleStringProperty("⭐".repeat(d.getRating())));
        addColumn(t, "Comment", 400, d -> new SimpleStringProperty(d.getComment()));
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return t;
    }

    private TableView<Product> createInventoryTable() {
        TableView<Product> t = new TableView<>(productList);
        addColumn(t, "ID", 60, d -> new SimpleObjectProperty<>(d.getProductId()));
        addColumn(t, "Product Name", 300, d -> new SimpleStringProperty(d.getName()));
        addColumn(t, "In Stock", 120, d -> new SimpleObjectProperty<>(d.getStockQuantity()));
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return t;
    }

    // --- DIALOGS & HANDLERS ---

    private void handleProductDialog(Product existing) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Product" : "Edit Product");
        ButtonType saveBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        TextField name = new TextField(existing != null ? existing.getName() : "");
        TextArea desc = new TextArea(existing != null ? existing.getDescription() : ""); desc.setPrefRowCount(3);
        TextField price = new TextField(existing != null ? String.valueOf(existing.getPrice()) : "");
        TextField stock = new TextField(existing != null ? String.valueOf(existing.getStockQuantity()) : "10");
        ComboBox<String> cat = new ComboBox<>(FXCollections.observableArrayList(categories.stream().map(Category::getName).toList()));
        if (existing != null) cat.setValue(existing.getCategoryName());

        grid.add(new Label("Name:"), 0, 0); grid.add(name, 1, 0);
        grid.add(new Label("Description:"), 0, 1); grid.add(desc, 1, 1);
        grid.add(new Label("Price:"), 0, 2); grid.add(price, 1, 2);
        grid.add(new Label("Stock:"), 0, 3); grid.add(stock, 1, 3);
        grid.add(new Label("Category:"), 0, 4); grid.add(cat, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                Product p = existing != null ? existing : new Product();
                p.setName(name.getText()); p.setDescription(desc.getText());
                p.setPrice(Double.parseDouble(price.getText()));
                p.setStockQuantity(Integer.parseInt(stock.getText()));
                String selectedCat = cat.getValue();
                categories.stream().filter(c -> c.getName().equals(selectedCat)).findFirst().ifPresent(c -> p.setCategoryId(c.getCategoryId()));
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            try {
                if (existing == null) productService.addProduct(p); else productService.updateProduct(p);
                loadInitialData();
            } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
        });
    }

    private void handleDeleteProduct(Product selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remove " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try { productService.deleteProduct(selected.getProductId()); loadInitialData(); }
                catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
            }
        });
    }

    private void handleCategoryDialog(Category existing) {
        TextInputDialog dialog = new TextInputDialog(existing != null ? existing.getName() : "");
        dialog.setTitle(existing == null ? "Add Category" : "Edit Category");
        dialog.setHeaderText("Enter category name:");
        dialog.showAndWait().ifPresent(name -> {
            try {
                if (existing == null) { Category c = new Category(); c.setName(name); categoryDAO.addCategory(c); }
                else { existing.setName(name); categoryDAO.updateCategory(existing); }
                loadInitialData();
            } catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
        });
    }

    private void handleDeleteCategory(Category selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remove category " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try { categoryDAO.deleteCategory(selected.getCategoryId()); loadInitialData(); }
                catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
            }
        });
    }

    private void handleInventoryDialog(Product product) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(product.getStockQuantity()));
        dialog.setTitle("Update Stock");
        dialog.setHeaderText("Update stock for: " + product.getName());
        dialog.setContentText("Quantity on hand:");
        dialog.showAndWait().ifPresent(qty -> {
            try {
                product.setStockQuantity(Integer.parseInt(qty));
                productService.updateProduct(product);
                loadInitialData();
            } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Error", "Invalid quantity"); }
        });
    }

    private void handleUserDialog(User existing) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New User" : "Edit User");
        ButtonType saveBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        TextField name = new TextField(existing != null ? existing.getName() : "");
        TextField email = new TextField(existing != null ? existing.getEmail() : "");
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("CUSTOMER", "ADMIN"));
        role.setValue(existing != null ? existing.getRole() : "CUSTOMER");

        grid.add(new Label("Name:"), 0, 0); grid.add(name, 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(email, 1, 1);
        grid.add(new Label("Role:"), 0, 2); grid.add(role, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                User u = existing != null ? existing : new User();
                u.setName(name.getText()); u.setEmail(email.getText()); u.setRole(role.getValue());
                return u;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(u -> {
            try {
                if (existing == null) userDAO.addUser(u); else userDAO.updateUser(u);
                loadUsers();
            } catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
        });
    }

    private void handleDeleteUser(User selected) {
        if ("ADMIN".equalsIgnoreCase(selected.getRole())) {
            showAlert(Alert.AlertType.WARNING, "Guard", "Administrator accounts cannot be removed from this console.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remove user " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try { userDAO.deleteUser(selected.getUserId()); loadUsers(); }
                catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
            }
        });
    }

    private void handleSeedData() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Categories (name) VALUES ('Smartphones'), ('Laptops'), ('Audio'), ('Wearables'), ('Accessories') ON CONFLICT DO NOTHING");
            
            String seedProducts = "INSERT INTO Products (name, description, price, category_id) VALUES " +
                "('iPhone 15 Pro', 'Titanium build, A17 Pro chip', 999.00, (SELECT category_id FROM Categories WHERE name='Smartphones')), " +
                "('Samsung S24 Ultra', 'AI features, S-Pen included', 1199.00, (SELECT category_id FROM Categories WHERE name='Smartphones')), " +
                "('MacBook Air M3', 'Liquid Retina display, fanless design', 1099.00, (SELECT category_id FROM Categories WHERE name='Laptops')), " +
                "('Dell XPS 13', 'InfinityEdge touch display', 949.00, (SELECT category_id FROM Categories WHERE name='Laptops')), " +
                "('Sony WH-1000XM5', 'Industry-leading noise canceling', 349.00, (SELECT category_id FROM Categories WHERE name='Audio')), " +
                "('AirPods Pro 2', 'USB-C charging branch', 249.00, (SELECT category_id FROM Categories WHERE name='Audio')), " +
                "('Apple Watch Ultra 2', 'Rugged outdoor smartwatch', 799.00, (SELECT category_id FROM Categories WHERE name='Wearables')), " +
                "('Pixel Watch 2', 'Fitbit integration included', 349.00, (SELECT category_id FROM Categories WHERE name='Wearables')), " +
                "('Anker 737 PowerBank', '140W fast charging 24K', 149.00, (SELECT category_id FROM Categories WHERE name='Accessories')), " +
                "('Logitech MX Master 3S', 'Ergonomic wireless mouse', 99.00, (SELECT category_id FROM Categories WHERE name='Accessories')) " +
                "ON CONFLICT DO NOTHING";
            stmt.execute(seedProducts);
            
            stmt.execute("INSERT INTO Inventory (product_id, quantity_on_hand) " +
                "SELECT product_id, 25 FROM Products ON CONFLICT (product_id) DO NOTHING");
            
            loadInitialData(); showAlert(Alert.AlertType.INFORMATION, "Success", "Premium sample data populated!");
        } catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Seed Failed", e.getMessage()); }
    }

    // --- DATA LOADERS ---

    private void loadInitialData() {
        try {
            categories = categoryDAO.getAllCategories();
            categoryList.setAll(categories);
            productList.setAll(productService.searchProducts("", null, 1, 500, null));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadUsers() { try { userList.setAll(userDAO.getAllUsers().stream().filter(u -> !"ADMIN".equals(u.getRole())).toList()); } catch (SQLException e) {} }
    private void loadOrders() { try { orderList.setAll(orderDAO.getAllOrders()); } catch (SQLException e) {} }
    private void loadReviews() { try { reviewList.setAll(reviewDAO.getAllReviews()); } catch (SQLException e) {} }
    private void loadInventory() { loadInitialData(); }

    // --- HELPERS ---
    private <T> void addColumn(TableView<T> t, String name, double width, java.util.function.Function<T, ObservableValue<?>> factory) {
        TableColumn<T, Object> col = new TableColumn<>(name);
        col.setPrefWidth(width);
        col.setCellValueFactory(data -> {
            ObservableValue<?> val = factory.apply(data.getValue());
            return (ObservableValue<Object>) val;
        });
        t.getColumns().add(col);
    }
    private void showAlert(Alert.AlertType t, String ti, String m) { Alert a = new Alert(t, m); a.setTitle(ti); a.show(); }
}
