package com.company.shop.module.user.dto;

/**
 * DTO zwracane użytkownikowi po pomyślnym uwierzytelnieniu.
 */
public class AuthResponseDTO {

	private final String token;
	private final String type = "Bearer";

	public AuthResponseDTO(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public String getType() {
		return type;
	}
}