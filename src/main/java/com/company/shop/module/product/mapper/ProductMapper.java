package com.company.shop.module.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.company.shop.module.product.dto.ProductResponseDTO;
import com.company.shop.module.product.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

	@Mapping(target = "categoryId", source = "category.id")
	@Mapping(target = "categoryName", source = "category.name")
	ProductResponseDTO toDto(Product product);
}
