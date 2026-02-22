package com.company.shop.module.user.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.module.user.dto.UserResponseDTO;
import com.company.shop.module.user.dto.UserUpdateDTO;
import com.company.shop.module.user.entity.User;
import com.company.shop.module.user.mapper.UserMapper;
import com.company.shop.module.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	private final UserRepository repository;
	private final UserMapper mapper;

	public UserServiceImpl(UserRepository repository, UserMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<UserResponseDTO> findAll(Pageable pageable) {
		return repository.findAll(pageable).map(mapper::toDto);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponseDTO findById(UUID id) {
		return repository.findWithRolesById(id).map(mapper::toDto)
				.orElseThrow(() -> new EntityNotFoundException("Użytkownik o ID: " + id + " nie został znaleziony"));
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponseDTO getCurrentUserProfile() {
		// Pobieramy login (email) z SecurityContextu, który ustawił
		// JwtAuthenticationFilter
		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		return repository.findByEmailWithRoles(email).map(mapper::toDto).orElseThrow(
				() -> new EntityNotFoundException("Nie znaleziono profilu zalogowanego użytkownika: " + email));
	}

	@Override
	public UserResponseDTO update(UUID id, UserUpdateDTO dto) {
		User user = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Nie można zaktualizować. Użytkownik nie istnieje."));

		user.setFirstName(dto.getFirstName());
		user.setLastName(dto.getLastName());

		// Dzięki @Transactional i mechanizmowi Dirty Checking, save() nie jest
		// wymagane,
		// ale jawne wywołanie jest dopuszczalne dla czytelności.
		return mapper.toDto(user);
	}

	@Override
	public void delete(UUID id) {
		User user = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Nie można usunąć. Użytkownik nie istnieje."));

		user.markDeleted();
	}

	@Override
	@Transactional(readOnly = true)
	public User getCurrentUserEntity() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		return repository.findByEmailWithRoles(email)
				.orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użytkownika: " + email));
	}
}