package com.company.shop.validation.annotation;

import com.company.shop.module.user.dto.RegisterRequestDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterRequestDTO> {

	@Override
	public boolean isValid(RegisterRequestDTO user, ConstraintValidatorContext context) {
		boolean isValid = user.getPassword() != null && user.getPassword().equals(user.getPasswordRepeat());

		if (!isValid) {
			context.disableDefaultConstraintViolation(); // Wyłączamy błąd ogólny
			context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
					.addPropertyNode("passwordRepeat") // Przypisujemy błąd do pola
					.addConstraintViolation();
		}
		return isValid;
	}
}