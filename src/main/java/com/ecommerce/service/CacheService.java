package com.ecommerce.service;

import com.ecommerce.model.Product;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CacheService manages an in-memory cache using a HashMap.
 * In-memory caching with HashMap mirrors a database hash index, providing O(1) average lookup time.
 */
public class CacheService {
    private final Map<String, List<Product>> cache = new HashMap<>();

    public List<Product> get(String key) {
        return cache.get(key);
    }

    public void put(String key, List<Product> products) {
        cache.put(key, products);
    }

    public void invalidateAll() {
        cache.clear();
    }

    public void invalidateByPattern(String pattern) {
        List<String> keysToRemove = cache.keySet().stream()
                .filter(key -> key.contains(pattern))
                .collect(Collectors.toList());
        keysToRemove.forEach(cache::remove);
    }
}
