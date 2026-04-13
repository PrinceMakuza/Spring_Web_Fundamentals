package com.ecommerce.service;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.model.User;
import com.ecommerce.util.UserContext;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

/**
 * AuthService handles authentication and secure password management.
 */
public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    // Constructor for testing
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user. If successful, populates UserContext.
     */
    public boolean login(String email, String password) throws SQLException {
        User user = userDAO.getUserByEmail(email);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            UserContext.setCurrentUserId(user.getUserId());
            UserContext.setCurrentUserName(user.getName());
            UserContext.setCurrentUserEmail(user.getEmail());
            UserContext.setCurrentUserRole(user.getRole());
            return true;
        }
        return false;
    }

    /**
     * Registers a new user with a hashed password.
     */
    public void register(String name, String email, String password, String role) throws SQLException {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role != null ? role : "CUSTOMER");
        user.setPassword(hashed);
        userDAO.addUser(user);
    }

    public void logout() {
        UserContext.clear();
    }
}
