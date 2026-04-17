package com.ecommerce.service;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.UserContext;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * AuthService handles authentication and secure password management.
 */
@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Authenticates a user. If successful, populates UserContext.
     */
    public void login(String email, String password) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        
        if (user.getPassword() == null) {
            throw new IllegalArgumentException("Invalid account configuration: missing password hash.");
        }
        
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password.");
        }
        
        UserContext.setCurrentUserId(user.getUserId());
        UserContext.setCurrentUserName(user.getName());
        UserContext.setCurrentUserEmail(user.getEmail());
        UserContext.setCurrentUserRole(user.getRole());
        UserContext.setCurrentUserLocation(user.getLocation());
    }

    /**
     * Registers a new user with a hashed password.
     */
    @Transactional
    public User register(String name, String email, String password, String role, String location) {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setName(name);
        user.setEmail(email.toLowerCase());
        user.setRole(role != null ? role : "CUSTOMER");
        user.setPassword(hashed);
        user.setLocation(location);
        return userRepository.save(user);
    }

    public void logout() {
        UserContext.clear();
    }

    /**
     * Updates the current user's profile and hashes the new password if provided.
     */
    @Transactional
    public User updateProfile(int userId, String name, String email, String location, String plainPassword) {
        return userRepository.findById(userId).map(user -> {
            user.setName(name);
            user.setEmail(email.toLowerCase());
            user.setLocation(location);
            
            if (plainPassword != null && !plainPassword.trim().isEmpty()) {
                user.setPassword(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
            }
            
            // Update local context if it's the current user
            if (UserContext.getCurrentUserId() == userId) {
                UserContext.setCurrentUserName(name);
                UserContext.setCurrentUserEmail(user.getEmail());
                UserContext.setCurrentUserLocation(location);
            }
            
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
