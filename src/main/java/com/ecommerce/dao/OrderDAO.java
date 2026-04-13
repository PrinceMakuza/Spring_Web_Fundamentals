package com.ecommerce.dao;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderDAO handles database operations for Orders and OrderItems.
 */
public class OrderDAO {

    /**
     * Retrieves all orders for a specific user.
     */
    public List<Order> getOrdersByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE user_id = ? ORDER BY order_id ASC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setStatus(rs.getString("status"));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    /**
     * Retrieves all items associated with a specific order.
     */
    public List<OrderItem> getOrderItems(int orderId) throws SQLException {
        String sql = "SELECT oi.*, p.name as product_name " +
                     "FROM OrderItems oi " +
                     "JOIN Products p ON oi.product_id = p.product_id " +
                     "WHERE oi.order_id = ?";
        
        List<OrderItem> items = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getInt("order_item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    items.add(item);
                }
            }
        }
        return items;
    }
    /**
     * Retrieves all orders in the system with customer names.
     */
    public List<Order> getAllOrders() throws SQLException {
        String sql = "SELECT o.*, u.name as user_name FROM Orders o " +
                     "JOIN Users u ON o.user_id = u.user_id " +
                     "ORDER BY o.order_id ASC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setUserId(rs.getInt("user_id"));
                order.setUserName(rs.getString("user_name"));
                order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(rs.getString("status"));
                orders.add(order);
            }
        }
        return orders;
    }
}
