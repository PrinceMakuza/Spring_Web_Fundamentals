package com.ecommerce.graphql;

import com.ecommerce.model.Category;
import com.ecommerce.service.CategoryService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL resolver for Category queries.
 */
@Controller
public class CategoryResolver {

    private final CategoryService categoryService;

    public CategoryResolver(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @QueryMapping
    public List<Category> categories() {
        return categoryService.getAllCategories();
    }
}
