package com.company.shop.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.user.dto.AuthResponseDTO;
import com.company.shop.module.user.dto.LoginRequestDTO;
import com.company.shop.module.user.dto.RegisterRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpointy logowania i rejestracji użytkowników.")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	@Operation(summary = "Logowanie użytkownika")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Użytkownik zalogowany poprawnie."),
			@ApiResponse(responseCode = "401", description = "Nieprawidłowe dane logowania.")
	})
	public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
		return authService.login(request);
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Rejestracja użytkownika")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Użytkownik zarejestrowany poprawnie."),
			@ApiResponse(responseCode = "409", description = "Użytkownik o podanym e-mailu już istnieje.")
	})
	public void register(@Valid @RequestBody RegisterRequestDTO request) {
		authService.register(request);
	}
}
