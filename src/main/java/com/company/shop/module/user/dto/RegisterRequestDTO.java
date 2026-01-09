package com.company.shop.module.user.dto;

import com.company.shop.validation.annotation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatches // Adnotacja sprawdzająca, czy hasła są identyczne
public class RegisterRequestDTO {

	@Email
	@NotBlank
	private String email;

	@NotBlank
	@Size(min = 8)
	private String password;

	@NotBlank
	private String passwordRepeat;

	@NotBlank
	private String firstName;

	@NotBlank
	private String lastName;

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getPasswordRepeat() {
		return passwordRepeat;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
}