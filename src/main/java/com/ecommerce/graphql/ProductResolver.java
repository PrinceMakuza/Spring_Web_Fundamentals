package com.ecommerce.graphql;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
public class ProductResolver {

    private final ProductService productService;

    public ProductResolver(ProductService productService) {
        this.productService = productService;
    }

    @QueryMapping
    public Page<Product> products(@Argument int page, @Argument int size,
                                  @Argument String name, @Argument Integer categoryId,
                                  @Argument Double minPrice, @Argument Double maxPrice) {
        return productService.getProducts(page, size, "name", "asc", name, categoryId, minPrice, maxPrice);
    }

    @QueryMapping
    public Optional<Product> product(@Argument int id) {
        return productService.getProductById(id);
    }

    @MutationMapping
    public Product createProduct(@Argument ProductInput input) {
        ProductDTO dto = new ProductDTO(null, input.name(), input.description(),
                input.price(), input.categoryId(), input.stockQuantity());
        return productService.createProduct(dto);
    }

    @MutationMapping
    public Product updateProduct(@Argument int id, @Argument ProductInput input) {
        ProductDTO dto = new ProductDTO(null, input.name(), input.description(),
                input.price(), input.categoryId(), input.stockQuantity());
        return productService.updateProduct(id, dto);
    }

    @MutationMapping
    public boolean deleteProduct(@Argument int id) {
        productService.deleteProduct(id);
        return true;
    }

    /**
     * Input record for GraphQL product mutations.
     */
    public record ProductInput(String name, String description, Double price,
                               Integer categoryId, Integer stockQuantity) {}
}