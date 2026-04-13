package com.ecommerce.dao;

import com.ecommerce.model.CartItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CartDAO handles database operations for Cart and CartItems tables.
 */
public class CartDAO {

    /**
     * Ensures a cart exists for the user and returns its ID.
     */
    public int getOrCreateCart(int userId) throws SQLException {
        String findSql = "SELECT cart_id FROM Cart WHERE user_id = ?";
        String insertSql = "INSERT INTO Cart (user_id) VALUES (?) RETURNING cart_id";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(findSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Adds an item to the cart or updates quantity if it already exists.
     */
    public void addItemToCart(int cartId, int productId, int quantity) throws SQLException {
        String sql = "INSERT INTO CartItems (cart_id, product_id, quantity) " +
                     "VALUES (?, ?, ?) " +
                     "ON CONFLICT (cart_id, product_id) " +
                     "DO UPDATE SET quantity = CartItems.quantity + EXCLUDED.quantity";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves all items in a user's cart.
     */
    public List<CartItem> getCartItems(int cartId) throws SQLException {
        String sql = "SELECT ci.*, p.name as product_name, p.price " +
                     "FROM CartItems ci " +
                     "JOIN Products p ON ci.product_id = p.product_id " +
                     "WHERE ci.cart_id = ? " +
                     "ORDER BY ci.cart_item_id ASC";

        List<CartItem> items = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setCartItemId(rs.getInt("cart_item_id"));
                    item.setCartId(rs.getInt("cart_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    // Price is fetched from Products table during retrieval in demo
                    item.setUnitPrice(rs.getDouble("price")); 
                    item.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
                    items.add(item);
                }
            }
        }
        return items;
    }

    public void updateQuantity(int cartItemId, int quantity) throws SQLException {
        String sql = "UPDATE CartItems SET quantity = ? WHERE cart_item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, cartItemId);
            pstmt.executeUpdate();
        }
    }

    public void removeItem(int cartItemId) throws SQLException {
        String sql = "DELETE FROM CartItems WHERE cart_item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartItemId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Clears all items from a cart.
     */
    public void clearCart(int cartId, Connection conn) throws SQLException {
        String sql = "DELETE FROM CartItems WHERE cart_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartId);
            pstmt.executeUpdate();
        }
    }
}
