package com.ecommerce.model;

import java.time.LocalDateTime;

/**
 * CartItem model representing an item in a user's shopping cart.
 */
public class CartItem {
    private int cartItemId;
    private int cartId;
    private int productId;
    private String productName; // Joined from Products
    private int quantity;
    private double unitPrice; // Captured at the time of adding to cart
    private LocalDateTime addedAt;

    public CartItem() {}

    public int getCartItemId() { return cartItemId; }
    public void setCartItemId(int cartItemId) { this.cartItemId = cartItemId; }

    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public double getSubtotal() {
        return quantity * unitPrice;
    }
}
