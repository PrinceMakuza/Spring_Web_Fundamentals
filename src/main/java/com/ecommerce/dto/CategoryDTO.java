package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryDTO(
    Integer categoryId,
    
    @NotBlank(message = "Category name is required")
    String name,
    
    String description
) {}
