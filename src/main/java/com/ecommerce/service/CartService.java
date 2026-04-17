package com.ecommerce.service;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.*;
import com.ecommerce.util.DataEventBus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CartService manages the shopping cart business logic.
 * Uses Spring @Transactional to ensure atomic checkout.
 * Now synchronized with DataEventBus for real-time UI updates.
 */
@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public CartService(CartItemRepository cartItemRepository, 
                       ProductRepository productRepository,
                       UserRepository userRepository, 
                       OrderRepository orderRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void addToCart(int userId, int productId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough stock available");
        }

        List<CartItem> existingItems = cartItemRepository.findByUser(user);
        Optional<CartItem> matchingItem = existingItems.stream()
                .filter(item -> item.getProduct().getProductId() == productId)
                .findFirst();

        if (matchingItem.isPresent()) {
            CartItem item = matchingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setUser(user);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            cartItemRepository.save(item);
        }
        DataEventBus.publish();
    }

    public List<CartItem> getCartItems(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cartItemRepository.findByUser(user);
    }

    @Transactional
    public void updateQuantity(int cartItemId, int quantity) {
        cartItemRepository.findById(cartItemId).ifPresent(item -> {
            if (quantity <= 0) {
                cartItemRepository.delete(item);
            } else {
                item.setQuantity(quantity);
                cartItemRepository.save(item);
            }
            DataEventBus.publish();
        });
    }

    @Transactional
    public void removeFromCart(int cartItemId) {
        cartItemRepository.deleteById(cartItemId);
        DataEventBus.publish();
    }

    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public boolean checkout(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<CartItem> items = cartItemRepository.findByUser(user);

        if (items.isEmpty()) return false;

        double totalAmount = items.stream().mapToDouble(CartItem::getSubtotal).sum();
        processCheckout(user, items, totalAmount);
        
        cartItemRepository.deleteByUser(user);
        DataEventBus.publish();
        return true;
    }

    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void checkoutSingleItem(int cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        User user = cartItem.getUser();
        processCheckout(user, List.of(cartItem), cartItem.getSubtotal());
        
        cartItemRepository.delete(cartItem);
        DataEventBus.publish();
    }

    private void processCheckout(User user, List<CartItem> items, double totalAmount) {
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setStatus("COMPLETED");
        order = orderRepository.save(order);

        for (CartItem cartItem : items) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            order.getItems().add(orderItem);
        }
    }
}
