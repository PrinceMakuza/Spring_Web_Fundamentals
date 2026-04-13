package com.ecommerce.dao;

import com.ecommerce.model.Review;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ReviewDAO handles database operations for the Reviews table.
 */
public class ReviewDAO {

    public void addReview(Review review) throws SQLException {
        String sql = "INSERT INTO Reviews (product_id, user_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, review.getProductId());
            pstmt.setInt(2, review.getUserId());
            pstmt.setInt(3, review.getRating());
            pstmt.setString(4, review.getComment());
            pstmt.executeUpdate();
        }
    }

    public List<Review> getReviewsByProduct(int productId) throws SQLException {
        String sql = "SELECT r.*, u.name as user_name FROM Reviews r " +
                     "JOIN Users u ON r.user_id = u.user_id " +
                     "WHERE r.product_id = ? ORDER BY r.review_id ASC";
        List<Review> reviews = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setReviewId(rs.getInt("review_id"));
                    r.setProductId(rs.getInt("product_id"));
                    r.setUserId(rs.getInt("user_id"));
                    r.setUserName(rs.getString("user_name"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setReviewDate(rs.getTimestamp("review_date").toLocalDateTime());
                    reviews.add(r);
                }
            }
        }
        return reviews;
    }
    /**
     * Retrieves all reviews in the system.
     */
    public List<Review> getAllReviews() throws SQLException {
        String sql = "SELECT r.*, u.name as user_name, p.name as product_name " +
                     "FROM Reviews r " +
                     "JOIN Users u ON r.user_id = u.user_id " +
                     "JOIN Products p ON r.product_id = p.product_id " +
                     "ORDER BY r.review_id ASC";
        List<Review> reviews = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Review r = new Review();
                r.setReviewId(rs.getInt("review_id"));
                r.setProductId(rs.getInt("product_id"));
                r.setProductName(rs.getString("product_name"));
                r.setUserId(rs.getInt("user_id"));
                r.setUserName(rs.getString("user_name"));
                r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment"));
                r.setReviewDate(rs.getTimestamp("review_date").toLocalDateTime());
                reviews.add(r);
            }
        }
        return reviews;
    }
}
