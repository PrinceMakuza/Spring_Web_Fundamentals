package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * ProductService layer sitting between controller and repository.
 * Integrates Spring Caching and Jpa Specifications for high performance.
 */
@Service("springProductService")
public class SpringProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public SpringProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Retrieve products with filtering, sorting, and pagination.
     * Uses Spring Cache to avoid redundant DB hits for the same search criteria.
     */
    @Cacheable(value = "products", key = "{#page, #size, #sortBy, #sortDir, #name, #categoryId, #minPrice, #maxPrice}")
    public Page<Product> getProducts(int page, int size, String sortBy, String sortDir,
                                      String name, Integer categoryId,
                                      Double minPrice, Double maxPrice) {
        // Map 'date' to 'createdAt' for sorting
        String sortField = sortBy.equalsIgnoreCase("date") ? "createdAt" : sortBy;
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = Specification.where(null);

        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("category").get("categoryId"), categoryId));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        return productRepository.findAll(spec, pageable);
    }

    @Cacheable(value = "product", key = "#id")
    public Optional<Product> getProductById(int id) {
        return productRepository.findById(id);
    }

    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public Product createProduct(ProductDTO dto) {
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.categoryId()));

        Product product = new Product();
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setCategory(category);
        product.setStockQuantity(dto.stockQuantity() != null ? dto.stockQuantity() : 0);
        return productRepository.save(product);
    }

    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public Product updateProduct(int id, ProductDTO dto) {
        return productRepository.findById(id).map(product -> {
            product.setName(dto.name());
            product.setDescription(dto.description());
            product.setPrice(dto.price());
            if (dto.categoryId() != null) {
                Category category = categoryRepository.findById(dto.categoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.categoryId()));
                product.setCategory(category);
            }
            if (dto.stockQuantity() != null) {
                product.setStockQuantity(dto.stockQuantity());
            }
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void deleteProduct(int id) {
        productRepository.deleteById(id);
    }
}
