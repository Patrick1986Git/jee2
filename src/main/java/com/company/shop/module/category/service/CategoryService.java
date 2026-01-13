package com.company.shop.module.category.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.company.shop.module.category.dto.CategoryCreateDTO;
import com.company.shop.module.category.dto.CategoryResponseDTO;

public interface CategoryService {

	Page<CategoryResponseDTO> findAll(Pageable pageable);

	CategoryResponseDTO findById(UUID id);

	CategoryResponseDTO findBySlug(String slug);

	CategoryResponseDTO create(CategoryCreateDTO dto);

	CategoryResponseDTO update(UUID id, CategoryCreateDTO dto);

	void delete(UUID id);
}