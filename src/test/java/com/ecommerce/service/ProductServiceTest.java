package com.ecommerce.service;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Ensure cache always misses
        when(cacheService.get(anyString())).thenReturn(null);
    }

    @Test
    void testSortingAZ() throws SQLException {
        List<Product> products = new ArrayList<>();
        Product p1 = new Product(); p1.setName("B");
        Product p2 = new Product(); p2.setName("A");
        products.add(p1); products.add(p2);

        when(productDAO.getProductsBySearch(any(), any(), anyInt(), anyInt())).thenReturn(products);

        List<Product> result = productService.searchProducts("", null, 1, 10, "Name (A-Z)");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getName());
        assertEquals("B", result.get(1).getName());
    }

    @Test
    void testSortingZA() throws SQLException {
        List<Product> products = new ArrayList<>();
        Product p1 = new Product(); p1.setName("A");
        Product p2 = new Product(); p2.setName("B");
        products.add(p1); products.add(p2);

        when(productDAO.getProductsBySearch(any(), any(), anyInt(), anyInt())).thenReturn(products);

        List<Product> result = productService.searchProducts("", null, 1, 10, "Name (Z-A)");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("B", result.get(0).getName());
        assertEquals("A", result.get(1).getName());
    }

    @Test
    void testSortingPriceLowHigh() throws SQLException {
        List<Product> products = new ArrayList<>();
        Product p1 = new Product(); p1.setPrice(100.0);
        Product p2 = new Product(); p2.setPrice(50.0);
        products.add(p1); products.add(p2);

        when(productDAO.getProductsBySearch(any(), any(), anyInt(), anyInt())).thenReturn(products);

        List<Product> result = productService.searchProducts("", null, 1, 10, "Price (Low to High)");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(50.0, result.get(0).getPrice());
        assertEquals(100.0, result.get(1).getPrice());
    }
}
