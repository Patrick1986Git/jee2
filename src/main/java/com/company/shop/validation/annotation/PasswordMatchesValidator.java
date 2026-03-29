package com.company.shop.validation.annotation;

import com.company.shop.module.user.dto.RegisterRequestDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterRequestDTO> {

	@Override
	public boolean isValid(RegisterRequestDTO user, ConstraintValidatorContext context) {
		if (user == null) {
			return true;
		}

		String password = user.getPassword();
		String passwordRepeat = user.getPasswordRepeat();

		if (isBlank(password) || isBlank(passwordRepeat)) {
			return true;
		}

		boolean isValid = password.equals(passwordRepeat);
		if (!isValid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
					.addPropertyNode("passwordRepeat")
					.addConstraintViolation();
		}

		return isValid;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
