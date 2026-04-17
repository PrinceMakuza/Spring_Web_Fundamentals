package com.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * User model representing a system user with RBAC.
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Role is required")
    private String role; // ADMIN or CUSTOMER

    @Size(min = 6, message = "Password must be at least 6 characters")
    @NotBlank(message = "Password is required")
    @Column(name = "password_hash", nullable = false)
    private String password;

    private String location;

    public User() {}

    public User(int userId, String name, String email, String role, String location) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.location = location;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
}
