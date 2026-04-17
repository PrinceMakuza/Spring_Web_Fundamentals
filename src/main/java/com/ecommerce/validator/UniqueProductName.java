package com.ecommerce.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueProductNameValidator.class)
@Documented
public @interface UniqueProductName {
    String message() default "Product name already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
