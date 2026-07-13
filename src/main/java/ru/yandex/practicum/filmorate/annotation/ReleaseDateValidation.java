package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReleaseDateValidationImpl.class)
public @interface ReleaseDateValidation {
    String startDate();

    String message() default "{ru.yandex.practicum.filmorate.annotation.ReleaseDateValidation}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}


