package com.ecommerce.dto;

import jakarta.validation.constraints.*;

public record UserDTO(
    Integer userId,
    
    @NotBlank(message = "Name is required")
    String name,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Role is required")
    String role,
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,
    
    String location
) {}
