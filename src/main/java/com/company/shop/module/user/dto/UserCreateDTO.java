package com.company.shop.module.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO używane do tworzenia nowego użytkownika (np. przez administratora).
 */
public class UserCreateDTO {

	@Email(message = "Niepoprawny format adresu email")
	@NotBlank(message = "Email nie może być pusty")
	private String email;

	@NotBlank(message = "Hasło nie może być puste")
	@Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków")
	private String password;

	@NotBlank(message = "Imię nie może być puste")
	@Size(max = 100, message = "Imię nie może przekraczać 100 znaków")
	private String firstName;

	@NotBlank(message = "Nazwisko nie może być puste")
	@Size(max = 100, message = "Nazwisko nie może przekraczać 100 znaków")
	private String lastName;

	// Pusty konstruktor - wymagany przez bibliotekę Jackson do deserializacji JSON
	public UserCreateDTO() {
	}

	public UserCreateDTO(String email, String password, String firstName, String lastName) {
		this.email = email;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
}