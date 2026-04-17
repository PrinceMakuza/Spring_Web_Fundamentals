package com.ecommerce.util;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.*;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds the database with default credentials and robust sample data.
 * Implements a "Clean Start" by clearing tables before seeding.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final CartItemRepository cartItemRepository;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           OrderRepository orderRepository,
                           ReviewRepository reviewRepository,
                           CartItemRepository cartItemRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("[DataInitializer] Initializing CLEAN START...");
        
        // Clear in order of dependencies
        reviewRepository.deleteAll();
        orderRepository.deleteAll(); // Cascades to OrderItems
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        
        System.out.println("[DataInitializer] Database cleared. Seeding fresh data...");

        seedCategories();
        seedUsers();
        seedProducts();

        System.out.println("[DataInitializer] Cleanup and seeding complete.");
    }

    private void seedCategories() {
        categoryRepository.saveAll(List.of(
            new Category(0, "Electronics", "Gadgets and electronic devices"),
            new Category(0, "Home & Kitchen", "Appliances and home decor"),
            new Category(0, "Books", "Educational and fictional books"),
            new Category(0, "Fashion", "Clothing and accessories")
        ));
    }

    private void seedUsers() {
        // Default Admin
        User admin = new User();
        admin.setName("System Admin");
        admin.setEmail("admin@ecommerce.com");
        admin.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
        admin.setRole("ADMIN");
        admin.setLocation("Headquarters");
        userRepository.save(admin);

        // Default Customer
        User customer = new User();
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()));
        customer.setRole("CUSTOMER");
        customer.setLocation("Accra, Ghana");
        userRepository.save(customer);
    }

    private void seedProducts() {
        List<Category> cats = categoryRepository.findAll();
        Category electronics = cats.stream().filter(c -> c.getName().equalsIgnoreCase("Electronics")).findFirst().orElse(null);
        Category home = cats.stream().filter(c -> c.getName().equalsIgnoreCase("Home & Kitchen")).findFirst().orElse(null);

        if (electronics != null) {
            productRepository.saveAll(List.of(
                new Product(0, "Laptop Pro", "High-performance laptop", 1299.99, electronics, 50),
                new Product(0, "Wireless Mouse", "Ergonomic mouse", 25.50, electronics, 200),
                new Product(0, "4K Monitor", "27-inch display", 349.99, electronics, 30)
            ));
        }

        if (home != null) {
            productRepository.saveAll(List.of(
                new Product(0, "Coffee Maker", "Drip coffee maker", 49.99, home, 40),
                new Product(0, "Air Fryer", "Oil-free cooker", 119.50, home, 25)
            ));
        }
    }
}
