package com.ecommerce.service;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.model.Product;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ProductService layer sits between controller and DAO.
 * Integrates caching, sorting, and business logic.
 */
public class ProductService {
    private final ProductDAO productDAO;
    private final CacheService cacheService;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.cacheService = new CacheService();
    }

    /**
     * Search and cache logic mirroring hash and B-tree index concepts.
     */
    public List<Product> searchProducts(String searchTerm, Integer categoryId, int page, int pageSize, String sortBy) throws SQLException {
        String cacheKey = String.format("search:%s:category:%d:page:%d:sort:%s", searchTerm, categoryId != null ? categoryId : -1, page, sortBy);
        
        // Check cache first
        List<Product> products = cacheService.get(cacheKey);
        if (products != null) {
            return products;
        }

        // Database fetch if not cached
        int offset = (page - 1) * pageSize;
        products = productDAO.getProductsBySearch(searchTerm, categoryId, offset, pageSize);

        // Apply sorting (mirrors B-Tree index for maintaining ordered data)
        applySorting(products, sortBy);

        // Put in cache
        cacheService.put(cacheKey, products);
        
        return products;
    }

    public void addProduct(Product product) throws SQLException {
        productDAO.addProduct(product);
        cacheService.invalidateAll(); // Invalidate cache after modification
    }

    public void updateProduct(Product product) throws SQLException {
        productDAO.updateProduct(product);
        cacheService.invalidateAll(); // Invalidate cache after modification
    }

    public void deleteProduct(int productId) throws SQLException {
        productDAO.deleteProduct(productId);
        cacheService.invalidateAll(); // Invalidate cache after modification
    }

    public List<String[]> getAllCategories() throws SQLException {
        return productDAO.getAllCategories();
    }

    public int getSearchCount(String searchTerm, Integer categoryId) throws SQLException {
        return productDAO.getSearchCount(searchTerm, categoryId);
    }

    private void applySorting(List<Product> products, String sortBy) {
        if (sortBy == null || products == null) return;
        
        switch (sortBy) {
            case "Name (A-Z)":
                Collections.sort(products, Comparator.comparing(Product::getName));
                break;
            case "Price (Low to High)":
                Collections.sort(products, Comparator.comparing(Product::getPrice));
                break;
            case "Price (High to Low)":
                Collections.sort(products, Comparator.comparing(Product::getPrice).reversed());
                break;
        }
    }
}
