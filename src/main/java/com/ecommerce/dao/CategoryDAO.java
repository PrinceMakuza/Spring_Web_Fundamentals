package com.ecommerce.dao;

import com.ecommerce.model.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Category CRUD operations.
 * All queries use PreparedStatement to prevent SQL injection.
 */
public class CategoryDAO {

    /**
     * Retrieves all categories ordered by name.
     */
    public List<Category> getAllCategories() throws SQLException {
        String sql = "SELECT category_id, name, description FROM Categories ORDER BY name";
        List<Category> categories = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Category cat = new Category();
                cat.setCategoryId(rs.getInt("category_id"));
                cat.setName(rs.getString("name"));
                cat.setDescription(rs.getString("description"));
                categories.add(cat);
            }
        }
        return categories;
    }

    /**
     * Adds a new category to the database.
     * Enforces unique name via database constraint.
     */
    public int addCategory(Category category) throws SQLException {
        String sql = "INSERT INTO Categories (name, description) VALUES (?, ?) RETURNING category_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Creating category failed, no ID obtained.");
    }

    /**
     * Updates an existing category.
     */
    public void updateCategory(Category category) throws SQLException {
        String sql = "UPDATE Categories SET name = ?, description = ? WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.setInt(3, category.getCategoryId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes a category by ID.
     * Will throw SQLException if products reference this category (ON DELETE RESTRICT).
     */
    public void deleteCategory(int categoryId) throws SQLException {
        String sql = "DELETE FROM Categories WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            pstmt.executeUpdate();
        }
    }
}
