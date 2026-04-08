package com.ecommerce.dao;

import com.ecommerce.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO handles all database operations for the Products table.
 * All queries use PreparedStatement (parameterized queries) to prevent SQL injection.
 *
 * Query patterns:
 * - ILIKE is used for case-insensitive search (PostgreSQL-specific)
 * - LIMIT/OFFSET for pagination (10 products per page)
 * - JOINs with Categories and Inventory tables for complete product data
 * - Transactions (auto-commit disabled) for atomic product + inventory operations
 */
public class ProductDAO {

    /**
     * Retrieves a page of products with category name and stock quantity.
     * Uses LIMIT/OFFSET for pagination.
     *
     * @param offset the starting row (0-based)
     * @param limit  the maximum number of rows to return
     */
    public List<Product> getProducts(int offset, int limit) throws SQLException {
        String sql = "SELECT p.*, c.name as category_name, COALESCE(i.quantity_on_hand, 0) as quantity_on_hand " +
                     "FROM Products p " +
                     "JOIN Categories c ON p.category_id = c.category_id " +
                     "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                     "ORDER BY p.product_id " +
                     "LIMIT ? OFFSET ?";

        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    /**
     * Searches products with case-insensitive name matching and optional category filter.
     * ILIKE provides case-insensitive pattern matching (User Story 3.1).
     *
     * @param searchTerm  the name search term (partial match)
     * @param categoryId  optional category filter (null for all categories)
     * @param offset      pagination offset
     * @param limit       pagination limit
     */
    public List<Product> getProductsBySearch(String searchTerm, Integer categoryId, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, c.name as category_name, COALESCE(i.quantity_on_hand, 0) as quantity_on_hand " +
            "FROM Products p " +
            "JOIN Categories c ON p.category_id = c.category_id " +
            "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
            "WHERE p.name ILIKE ? "
        );

        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
        }

        sql.append("ORDER BY p.product_id LIMIT ? OFFSET ?");

        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            pstmt.setString(paramIndex++, "%" + searchTerm + "%");
            if (categoryId != null && categoryId > 0) {
                pstmt.setInt(paramIndex++, categoryId);
            }
            pstmt.setInt(paramIndex++, limit);
            pstmt.setInt(paramIndex++, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    /**
     * Returns the total count of all products.
     */
    public int getProductCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Products";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Returns the count of products matching a search term and optional category filter.
     */
    public int getSearchCount(String searchTerm, Integer categoryId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Products p WHERE p.name ILIKE ? ");
        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ?");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setString(1, "%" + searchTerm + "%");
            if (categoryId != null && categoryId > 0) {
                pstmt.setInt(2, categoryId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Adds a new product and its inventory record in a single transaction.
     * Uses RETURNING clause (PostgreSQL) to get the generated product ID.
     *
     * @param product the product to add
     * @return the generated product_id
     */
    public int addProduct(Product product) throws SQLException {
        String productSql = "INSERT INTO Products (name, description, price, category_id) VALUES (?, ?, ?, ?) RETURNING product_id";
        String inventorySql = "INSERT INTO Inventory (product_id, quantity_on_hand) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int productId;
                try (PreparedStatement pstmt = conn.prepareStatement(productSql)) {
                    pstmt.setString(1, product.getName());
                    pstmt.setString(2, product.getDescription());
                    pstmt.setDouble(3, product.getPrice());
                    pstmt.setInt(4, product.getCategoryId());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            productId = rs.getInt(1);
                        } else {
                            throw new SQLException("Creating product failed, no ID obtained.");
                        }
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(inventorySql)) {
                    pstmt.setInt(1, productId);
                    pstmt.setInt(2, product.getStockQuantity());
                    pstmt.executeUpdate();
                }

                conn.commit();
                return productId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Updates an existing product and its inventory record in a single transaction.
     * Uses PostgreSQL UPSERT (ON CONFLICT DO UPDATE) for the inventory record.
     */
    public void updateProduct(Product product) throws SQLException {
        String productSql = "UPDATE Products SET name = ?, description = ?, price = ?, category_id = ? WHERE product_id = ?";
        String inventorySql = "INSERT INTO Inventory (product_id, quantity_on_hand) VALUES (?, ?) " +
                             "ON CONFLICT (product_id) DO UPDATE SET quantity_on_hand = EXCLUDED.quantity_on_hand";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pstmt = conn.prepareStatement(productSql)) {
                    pstmt.setString(1, product.getName());
                    pstmt.setString(2, product.getDescription());
                    pstmt.setDouble(3, product.getPrice());
                    pstmt.setInt(4, product.getCategoryId());
                    pstmt.setInt(5, product.getProductId());
                    pstmt.executeUpdate();
                }

                try (PreparedStatement pstmt = conn.prepareStatement(inventorySql)) {
                    pstmt.setInt(1, product.getProductId());
                    pstmt.setInt(2, product.getStockQuantity());
                    pstmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Deletes a product by ID.
     * Will fail if OrderItems reference this product (ON DELETE RESTRICT).
     */
    public void deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM Products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Returns all categories as String arrays [id, name].
     * Kept for backward compatibility with services that don't use Category model.
     */
    public List<String[]> getAllCategories() throws SQLException {
        String sql = "SELECT category_id, name FROM Categories ORDER BY name";
        List<String[]> categories = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                categories.add(new String[]{String.valueOf(rs.getInt("category_id")), rs.getString("name")});
            }
        }
        return categories;
    }

    /**
     * Maps a ResultSet row to a Product object.
     * Handles the joined category_name and quantity_on_hand fields.
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getDouble("price"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setStockQuantity(rs.getInt("quantity_on_hand"));
        return p;
    }
}
