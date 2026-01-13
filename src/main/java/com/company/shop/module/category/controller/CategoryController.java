package com.company.shop.module.category.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.category.dto.CategoryCreateDTO;
import com.company.shop.module.category.dto.CategoryResponseDTO;
import com.company.shop.module.category.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/categories")
public class CategoryController {

	private final CategoryService service;

	public CategoryController(CategoryService service) {
		this.service = service;
	}

	@GetMapping
	public Page<CategoryResponseDTO> getAll(@PageableDefault(size = 20) Pageable pageable) {
		return service.findAll(pageable);
	}

	@GetMapping("/slug/{slug}")
	public CategoryResponseDTO getBySlug(@PathVariable String slug) {
		return service.findBySlug(slug);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public CategoryResponseDTO getById(@PathVariable UUID id) {
		return service.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public CategoryResponseDTO create(@Valid @RequestBody CategoryCreateDTO dto) {
		return service.create(dto);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public CategoryResponseDTO update(@PathVariable UUID id, @Valid @RequestBody CategoryCreateDTO dto) {
		return service.update(id, dto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	public void delete(@PathVariable UUID id) {
		service.delete(id);
	}
}