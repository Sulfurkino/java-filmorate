package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReleaseDateValidationImpl
        implements ConstraintValidator<ReleaseDateValidation, LocalDate> {

    private LocalDate startDate;

    @Override
    public void initialize(ReleaseDateValidation annotation) {
        startDate = LocalDate.parse(
                annotation.startDate(),
                DateTimeFormatter.ofPattern("yyyy.MM.dd")
        );
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }

        return !value.isBefore(startDate);
    }
}
