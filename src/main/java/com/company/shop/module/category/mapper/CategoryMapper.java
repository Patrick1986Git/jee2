package com.company.shop.module.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.company.shop.module.category.dto.CategoryResponseDTO;
import com.company.shop.module.category.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

	/**
	 * Mapuje encję Category na CategoryResponseDTO. Automatycznie wyciąga nazwę
	 * kategorii nadrzędnej (parent.name) i przypisuje ją do pola parentName.
	 */
	@Mapping(target = "parentName", source = "parent.name")
	CategoryResponseDTO toDto(Category category);
}