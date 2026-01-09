package com.company.shop.module.user.dto;

import java.util.Set;
import java.util.UUID;

/**
 * DTO służące do przesyłania danych użytkownika do klienta (Frontend).
 * Bezpiecznie pomija wrażliwe dane, takie jak hasło.
 */
public class UserResponseDTO {

	private final UUID id;
	private final String email;
	private final String firstName;
	private final String lastName;
	private final Set<String> roles;

	public UserResponseDTO(UUID id, String email, String firstName, String lastName, Set<String> roles) {
		this.id = id;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.roles = roles;
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public Set<String> getRoles() {
		return roles;
	}
}