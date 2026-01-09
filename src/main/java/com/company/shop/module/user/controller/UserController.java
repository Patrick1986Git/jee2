package com.company.shop.module.user.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.user.dto.UserResponseDTO;
import com.company.shop.module.user.dto.UserUpdateDTO;
import com.company.shop.module.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService service;

	public UserController(UserService service) {
		this.service = service;
	}

	/**
	 * Endpoint dla zalogowanego użytkownika - pobiera własny profil. Dostępny dla
	 * każdego uwierzytelnionego użytkownika.
	 */
	@GetMapping("/me")
	public UserResponseDTO getCurrentUser() {
		return service.getCurrentUserProfile();
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Page<UserResponseDTO> getAll(@PageableDefault(size = 20) Pageable pageable) {
		return service.findAll(pageable);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponseDTO getById(@PathVariable UUID id) {
		return service.findById(id);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponseDTO update(@PathVariable UUID id, @Valid @RequestBody UserUpdateDTO dto) {
		return service.update(id, dto);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public void delete(@PathVariable UUID id) {
		service.delete(id);
	}
}