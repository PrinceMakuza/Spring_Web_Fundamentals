package com.ecommerce.dto;

import com.ecommerce.validator.UniqueProductName;
import jakarta.validation.constraints.*;

public record ProductDTO(
    Integer productId,
    
    @NotBlank(message = "Product name is required")
    @UniqueProductName
    String name,
    
    String description,
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be greater than or equal to 0")
    Double price,
    
    @NotNull(message = "Category ID is required")
    Integer categoryId,
    
    @Min(value = 0, message = "Stock quantity cannot be negative")
    Integer stockQuantity
) {}
