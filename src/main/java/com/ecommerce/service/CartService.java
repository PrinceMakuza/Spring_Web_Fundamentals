package com.ecommerce.service;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.DatabaseConnection;
import com.ecommerce.model.CartItem;
import java.sql.*;
import java.util.List;

/**
 * CartService manages the shopping cart business logic.
 * Handles checkout as an atomic transaction to ensure data integrity.
 */
public class CartService {
    private final CartDAO cartDAO;

    public CartService() {
        this.cartDAO = new CartDAO();
    }

    public void addToCart(int userId, int productId, int quantity) throws SQLException {
        // Real-world logic: Proactive Stock Validation
        com.ecommerce.dao.ProductDAO productDAO = new com.ecommerce.dao.ProductDAO();
        com.ecommerce.model.Product p = productDAO.getProductById(productId);
        
        if (p == null) throw new SQLException("Product not found.");
        if (p.getStockQuantity() < quantity) {
            throw new SQLException("Cannot add to cart: Insufficient stock (" + p.getStockQuantity() + " available)");
        }

        int cartId = cartDAO.getOrCreateCart(userId);
        cartDAO.addItemToCart(cartId, productId, quantity);
    }

    public List<CartItem> getCartItems(int userId) throws SQLException {
        int cartId = cartDAO.getOrCreateCart(userId);
        return cartDAO.getCartItems(cartId);
    }

    public void updateQuantity(int cartItemId, int quantity) throws SQLException {
        if (quantity <= 0) {
            cartDAO.removeItem(cartItemId);
        } else {
            cartDAO.updateQuantity(cartItemId, quantity);
        }
    }

    public void removeFromCart(int cartItemId) throws SQLException {
        cartDAO.removeItem(cartItemId);
    }

    /**
     * Checkout process converts cart items to an order and updates inventory.
     * Implements User Story 4.2: Atomicity via JDBC Transactions.
     */
    public boolean checkout(int userId) throws SQLException {
        int cartId = cartDAO.getOrCreateCart(userId);
        List<CartItem> items = cartDAO.getCartItems(cartId);
        
        if (items.isEmpty()) return false;

        double totalAmount = items.stream().mapToDouble(CartItem::getSubtotal).sum();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Create Order
                int orderId;
                String orderSql = "INSERT INTO Orders (user_id, total_amount, status) VALUES (?, ?, 'COMPLETED') RETURNING order_id";
                try (PreparedStatement pstmt = conn.prepareStatement(orderSql)) {
                    pstmt.setInt(1, userId);
                    pstmt.setDouble(2, totalAmount);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) orderId = rs.getInt(1);
                        else throw new SQLException("Failed to create order.");
                    }
                }

                // 2. Process each item
                String itemSql = "INSERT INTO OrderItems (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
                String inventorySql = "UPDATE Inventory SET quantity_on_hand = quantity_on_hand - ? WHERE product_id = ? AND quantity_on_hand >= ?";
                
                try (PreparedStatement itemPstmt = conn.prepareStatement(itemSql);
                     PreparedStatement invPstmt = conn.prepareStatement(inventorySql)) {
                    
                    for (CartItem item : items) {
                        // Add to OrderItems
                        itemPstmt.setInt(1, orderId);
                        itemPstmt.setInt(2, item.getProductId());
                        itemPstmt.setInt(3, item.getQuantity());
                        itemPstmt.setDouble(4, item.getUnitPrice());
                        itemPstmt.addBatch();

                        // Deduct Inventory
                        invPstmt.setInt(1, item.getQuantity());
                        invPstmt.setInt(2, item.getProductId());
                        invPstmt.setInt(3, item.getQuantity()); // Ensure enough stock
                        int updated = invPstmt.executeUpdate();
                        
                        if (updated == 0) {
                            throw new SQLException("Insufficient stock for product ID: " + item.getProductId());
                        }
                    }
                    itemPstmt.executeBatch();
                }

                // 3. Clear Cart
                cartDAO.clearCart(cartId, conn);

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
