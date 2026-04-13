package com.ecommerce.service;

import com.ecommerce.dao.OrderDAO;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import java.sql.SQLException;
import java.util.List;

/**
 * OrderService manages order history business logic.
 */
public class OrderService {
    private final OrderDAO orderDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
    }

    public List<Order> getUserOrderHistory(int userId) throws SQLException {
        return orderDAO.getOrdersByUser(userId);
    }

    public List<OrderItem> getOrderDetails(int orderId) throws SQLException {
        return orderDAO.getOrderItems(orderId);
    }
}
