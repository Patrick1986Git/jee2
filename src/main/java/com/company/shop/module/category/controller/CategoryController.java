package com.company.shop.module.category.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.category.dto.CategoryResponseDTO;
import com.company.shop.module.category.service.CategoryService;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

	private final CategoryService service;

	public CategoryController(CategoryService service) {
		this.service = service;
	}

	@GetMapping
	public Page<CategoryResponseDTO> getCategories(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return service.findAll(pageable);
	}

	@GetMapping("/slug/{slug}")
	public CategoryResponseDTO getCategoryBySlug(@PathVariable String slug) {
		return service.findBySlug(slug);
	}
}