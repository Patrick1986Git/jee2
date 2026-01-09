package com.company.shop.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserUpdateDTO {

	@NotBlank(message = "Imię nie może być puste")
	@Size(max = 100, message = "Imię nie może przekraczać 100 znaków")
	private String firstName;

	@NotBlank(message = "Nazwisko nie może być puste")
	@Size(max = 100, message = "Nazwisko nie może przekraczać 100 znaków")
	private String lastName;

	// Pusty konstruktor dla biblioteki Jackson
	public UserUpdateDTO() {
	}

	public UserUpdateDTO(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
}