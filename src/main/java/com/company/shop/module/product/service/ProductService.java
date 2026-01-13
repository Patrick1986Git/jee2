package com.company.shop.module.product.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.company.shop.module.product.dto.ProductCreateDTO;
import com.company.shop.module.product.dto.ProductResponseDTO;

public interface ProductService {
	Page<ProductResponseDTO> findAll(Pageable pageable);

	Page<ProductResponseDTO> findAllByCategory(UUID categoryId, Pageable pageable);

	ProductResponseDTO findById(UUID id);

	ProductResponseDTO findBySlug(String slug);

	ProductResponseDTO create(ProductCreateDTO dto);

	ProductResponseDTO update(UUID id, ProductCreateDTO dto);

	void delete(UUID id);
}