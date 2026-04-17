package com.ecommerce.validator;

import com.ecommerce.repository.ProductRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueProductNameValidator implements ConstraintValidator<UniqueProductName, String> {

    private final ProductRepository productRepository;

    public UniqueProductNameValidator(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.isBlank()) {
            return true;
        }
        return !productRepository.existsByName(name);
    }
}
