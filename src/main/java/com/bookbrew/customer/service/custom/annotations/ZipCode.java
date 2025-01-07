package com.bookbrew.customer.service.custom.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = ZipCodeValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ZipCode {

    String message() default "Invalid zip code format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}