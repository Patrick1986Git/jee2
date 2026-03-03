package com.company.shop.module.user.dto;

import com.company.shop.validation.annotation.PasswordMatches;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatches
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

	public RegisterRequestDTO() {
	}

	public RegisterRequestDTO(String email, String password, String passwordRepeat, String firstName, String lastName) {
		this.email = email;
		this.password = password;
		this.passwordRepeat = passwordRepeat;
		this.firstName = firstName;
		this.lastName = lastName;
	}

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
