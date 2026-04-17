package com.ecommerce.service;

import com.ecommerce.util.SpringContextBridge;
import com.ecommerce.util.UserContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuthServiceLoginTest {

    @Autowired
    private AuthService authService;

    @Test
    public void testAdminLogin() {
        UserContext.clear();
        authService.login("admin@ecommerce.com", "admin123");
        
        assertTrue(UserContext.isLoggedIn(), "Admin should be logged in");
        assertTrue(UserContext.isAdmin(), "User should have ADMIN role");
        assertEquals("admin@ecommerce.com", UserContext.getCurrentUserEmail());
    }

    @Test
    public void testCustomerLogin() {
        UserContext.clear();
        authService.login("john@example.com", "password123");
        
        assertTrue(UserContext.isLoggedIn(), "Customer should be logged in");
        assertTrue(UserContext.isCustomer(), "User should have CUSTOMER role");
        assertEquals("john@example.com", UserContext.getCurrentUserEmail());
    }

    @Test
    public void testInvalidLogin() {
        UserContext.clear();
        assertThrows(IllegalArgumentException.class, () -> {
            authService.login("john@example.com", "wrongpassword");
        });
        assertFalse(UserContext.isLoggedIn(), "User should not be logged in after failed attempt");
    }
}
