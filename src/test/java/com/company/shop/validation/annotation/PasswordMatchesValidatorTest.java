package com.company.shop.validation.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.company.shop.module.user.dto.RegisterRequestDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class PasswordMatchesValidatorTest {

    private ValidatorFactory validatorFactory;
    private Validator validator;
    private final PasswordMatchesValidator passwordMatchesValidator = new PasswordMatchesValidator();

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
    void passwordMatches_shouldPassWhenPasswordsAreEqual() {
        RegisterRequestDTO request = new RegisterRequestDTO("john@example.com", "secret123", "secret123", "John", "Doe");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("passwordRepeat")
                && v.getMessage().equals("Hasła nie są identyczne"));
    }

    @Test
    void passwordMatches_shouldFailWhenPasswordsAreDifferent() {
        RegisterRequestDTO request = new RegisterRequestDTO("john@example.com", "secret123", "secret124", "John", "Doe");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("passwordRepeat");
                    assertThat(violation.getMessage()).isEqualTo("Hasła nie są identyczne");
                });
    }

    @Test
    void passwordMatches_shouldFailWhenPasswordsDifferOnlyByCase() {
        RegisterRequestDTO request = new RegisterRequestDTO("john@example.com", "Secret123", "secret123", "John", "Doe");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("passwordRepeat");
                    assertThat(violation.getMessage()).isEqualTo("Hasła nie są identyczne");
                });
    }

    @Test
    void passwordMatches_shouldNotAddMismatchWhenPasswordIsNull() {
        RegisterRequestDTO request = new RegisterRequestDTO("john@example.com", null, "secret123", "John", "Doe");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .filteredOn(violation -> violation.getMessage().equals("Hasła nie są identyczne"))
                .isEmpty();
    }

    @Test
    void passwordMatches_shouldNotAddMismatchWhenPasswordRepeatIsBlank() {
        RegisterRequestDTO request = new RegisterRequestDTO("john@example.com", "secret123", "   ", "John", "Doe");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .filteredOn(violation -> violation.getMessage().equals("Hasła nie są identyczne"))
                .isEmpty();
    }

    @Test
    void isValid_shouldReturnTrueWhenRootDtoIsNull() {
        boolean isValid = passwordMatchesValidator.isValid(null, null);

        assertThat(isValid).isTrue();
    }

    @Test
    void passwordMatches_shouldAttachMismatchErrorToPasswordRepeatField() {
        RegisterRequestDTO request = new RegisterRequestDTO("john@example.com", "secret123", "different", "John", "Doe");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .filteredOn(violation -> violation.getMessage().equals("Hasła nie są identyczne"))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsOnly("passwordRepeat");
    }
}
