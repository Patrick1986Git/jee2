package com.company.shop.module.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class RegisterRequestDTOValidationTest {

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void validate_shouldReturnEmailViolationWhenEmailHasInvalidFormat() {
        RegisterRequestDTO request = new RegisterRequestDTO("invalid-email", "secret123", "secret123", "John", "Doe");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .filteredOn(violation -> violation.getPropertyPath().toString().equals("email"))
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("well-formed email"));
    }

    @Test
    void validate_shouldReturnSizeViolationWhenPasswordIsTooShort() {
        RegisterRequestDTO request = new RegisterRequestDTO("john@example.com", "short", "short", "John", "Doe");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .filteredOn(violation -> violation.getPropertyPath().toString().equals("password"))
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("size must be between 8"));
    }

    @Test
    void validate_shouldReturnNotBlankViolationsWhenFieldsAreEmptyStrings() {
        RegisterRequestDTO request = new RegisterRequestDTO("", "", "", "", "");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .filteredOn(violation -> violation.getMessage().contains("must not be blank"))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email", "password", "passwordRepeat", "firstName", "lastName");

        assertThat(violations)
                .filteredOn(violation -> violation.getMessage().equals("Hasła nie są identyczne"))
                .isEmpty();
    }

    @Test
    void validate_shouldReturnNotBlankViolationsWhenFieldsContainOnlyBlankSpaces() {
        RegisterRequestDTO request = new RegisterRequestDTO(" ", " ", " ", " ", " ");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .filteredOn(violation -> violation.getMessage().contains("must not be blank"))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email", "password", "passwordRepeat", "firstName", "lastName");

        assertThat(violations)
                .filteredOn(violation -> violation.getMessage().equals("Hasła nie są identyczne"))
                .isEmpty();
    }

    @Test
    void validate_shouldReturnFieldViolationsWhenAllFieldsAreNull() {
        RegisterRequestDTO request = new RegisterRequestDTO(null, null, null, null, null);

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email", "password", "passwordRepeat", "firstName", "lastName");

        assertThat(violations)
                .filteredOn(violation -> violation.getPropertyPath().toString().equals("password"))
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("must not be blank"));

        assertThat(violations)
                .filteredOn(violation -> violation.getMessage().equals("Hasła nie są identyczne"))
                .isEmpty();
    }

    @Test
    void validate_shouldAggregateErrorsFromMultipleFieldsInSingleValidationPass() {
        RegisterRequestDTO request = new RegisterRequestDTO("invalid-email", "short", "different", "", "");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email", "password", "passwordRepeat", "firstName", "lastName");

        assertThat(violations)
                .filteredOn(violation -> violation.getPropertyPath().toString().equals("password"))
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("size must be between 8"));

        assertThat(violations)
                .filteredOn(violation -> violation.getPropertyPath().toString().equals("passwordRepeat"))
                .extracting(ConstraintViolation::getMessage)
                .contains("Hasła nie są identyczne");
    }
}
