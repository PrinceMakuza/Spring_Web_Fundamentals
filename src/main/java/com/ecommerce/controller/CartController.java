package com.ecommerce.controller;

import com.ecommerce.model.CartItem;
import com.ecommerce.service.CartService;
import com.ecommerce.util.SpringContextBridge;
import com.ecommerce.util.UserContext;
import com.ecommerce.util.DataEventBus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CartController handles shopping cart interactions and layout.
 * Updated with quantity +/- buttons and individual item checkout.
 */
public class CartController {
    private final CartService cartService = SpringContextBridge.getBean(CartService.class);
    
    @FXML private VBox itemsContainer;
    @FXML private Label totalLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    
    private List<CartItem> allItems;

    @FXML
    public void initialize() {
        if (sortCombo != null) {
            sortCombo.setItems(FXCollections.observableArrayList("Name (A-Z)", "Price (Low to High)", "Price (High to Low)"));
        }
        
        // Subscribe to real-time sync
        DataEventBus.subscribe(this::loadCart);
        
        loadCart();
    }

    @FXML
    public void loadCart() {
        try {
            allItems = cartService.getCartItems(UserContext.getCurrentUserId());
            filterAndDisplay();
        } catch (Exception e) {}
    }

    @FXML
    private void filterAndDisplay() {
        itemsContainer.getChildren().clear();
        if (allItems == null) return;

        String search = searchField.getText().toLowerCase();
        List<CartItem> filtered = allItems.stream()
            .filter(i -> i.getProductName().toLowerCase().contains(search))
            .collect(Collectors.toList());

        String sort = sortCombo.getValue();
        if (sort != null) {
            if (sort.equals("Name (A-Z)")) filtered.sort(java.util.Comparator.comparing(CartItem::getProductName));
            else if (sort.equals("Price (Low to High)")) filtered.sort(java.util.Comparator.comparing(CartItem::getUnitPrice));
            else if (sort.equals("Price (High to Low)")) filtered.sort(java.util.Comparator.comparing(CartItem::getUnitPrice).reversed());
        }

        double total = 0;
        for (CartItem item : filtered) {
            total += item.getSubtotal();
            itemsContainer.getChildren().add(buildItemRow(item));
        }

        totalLabel.setText(String.format("Total: $%.2f", total));

        if (filtered.isEmpty()) {
            Label emptyLabel = new Label(allItems.isEmpty() ? "Your cart is empty." : "No matching items found.");
            emptyLabel.getStyleClass().add("label-muted");
            emptyLabel.setStyle("-fx-font-size: 16px;");
            itemsContainer.getChildren().add(emptyLabel);
        }
    }

    private HBox buildItemRow(CartItem item) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15));
        row.getStyleClass().add("card");

        VBox details = new VBox(5);
        Label name = new Label(item.getProductName());
        name.getStyleClass().add("label-bright");
        name.setFont(Font.font("System", FontWeight.BOLD, 18));
        Label price = new Label(String.format("$%.2f each", item.getUnitPrice()));
        price.getStyleClass().add("label-muted");
        details.getChildren().addAll(name, price);
        HBox.setHgrow(details, Priority.ALWAYS);

        // Quantity Controls [-] [Qty] [+]
        HBox qtyBox = new HBox(5);
        qtyBox.setAlignment(Pos.CENTER);
        Button minBtn = new Button("-");
        minBtn.setStyle("-fx-font-weight: bold; -fx-min-width: 30;");
        minBtn.setOnAction(e -> updateQty(item.getCartItemId(), item.getQuantity() - 1));
        
        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-min-width: 30; -fx-alignment: center;");
        
        Button plusBtn = new Button("+");
        plusBtn.setStyle("-fx-font-weight: bold; -fx-min-width: 30;");
        plusBtn.setOnAction(e -> updateQty(item.getCartItemId(), item.getQuantity() + 1));
        
        qtyBox.getChildren().addAll(minBtn, qtyLabel, plusBtn);

        Label subtotal = new Label(String.format("$%.2f", item.getSubtotal()));
        subtotal.getStyleClass().add("label-bright");
        subtotal.setStyle("-fx-text-fill: #38b86c; -fx-font-weight: bold; -fx-min-width: 100; -fx-alignment: center-right;");

        // Action Buttons
        Button buyNowBtn = new Button("Buy Now");
        buyNowBtn.getStyleClass().add("button-success");
        buyNowBtn.setStyle("-fx-font-size: 11px;");
        buyNowBtn.setOnAction(e -> {
            try {
                cartService.checkoutSingleItem(item.getCartItemId());
                showInfo("Success", "Ordered " + item.getProductName() + " successfully!");
            } catch (Exception ex) {
                showError("Order Failed", ex.getMessage());
            }
        });

        Button removeBtn = new Button("🗑");
        removeBtn.getStyleClass().add("button-danger");
        removeBtn.setOnAction(e -> {
            try {
                cartService.removeFromCart(item.getCartItemId());
            } catch (Exception ex) {
                showError("Error", ex.getMessage());
            }
        });

        row.getChildren().addAll(details, qtyBox, subtotal, buyNowBtn, removeBtn);
        return row;
    }

    private void updateQty(int itemId, int newQty) {
        try {
            if (newQty <= 0) {
                cartService.removeFromCart(itemId);
            } else {
                cartService.updateQuantity(itemId, newQty);
            }
        } catch (Exception e) {
            showError("Update Error", "Update failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckout() {
        try {
            if (cartService.checkout(UserContext.getCurrentUserId())) {
                showInfo("Success", "Full order placed successfully!");
            } else {
                showError("Checkout", "Your cart is empty.");
            }
        } catch (Exception e) {
            showError("Checkout Error", e.getMessage());
        }
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setTitle(title); a.setHeaderText(null); a.show();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setTitle(title); a.show();
    }
}
