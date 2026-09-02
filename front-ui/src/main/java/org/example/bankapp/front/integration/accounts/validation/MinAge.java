package org.example.bankapp.front.integration.accounts.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MinAgeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinAge {
    int value() default 18;
    String message() default "Возраст должен быть не менее {value} лет";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
