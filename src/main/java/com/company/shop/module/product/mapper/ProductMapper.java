/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.product.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.company.shop.module.product.dto.ProductResponseDTO;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.product.entity.ProductImage;

/**
 * Enterprise-grade mapper for product data transformations.
 * <p>
 * This component facilitates the mapping between {@link Product} entities 
 * and their respective DTOs, including complex flattening of product categories 
 * and media galleries.
 * </p>
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /**
     * Maps a {@link Product} entity to a comprehensive {@link ProductResponseDTO}.
     * <p>
     * Performs field flattening for category details and utilizes a specialized 
     * mapping strategy for the product gallery.
     * </p>
     *
     * @param product the source product aggregate.
     * @return the mapped response DTO.
     */
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "imageUrls", source = "images", qualifiedByName = "mapImagesToUrls")
    ProductResponseDTO toDto(Product product);

    /**
     * Specialized mapping logic to transform image entities into simple URL strings.
     * <p>
     * Ensures null-safety by returning an empty list if the source collection is missing.
     * </p>
     *
     * @param images the list of {@link ProductImage} entities.
     * @return a list of image URL strings.
     */
    @Named("mapImagesToUrls")
    default List<String> mapImagesToUrls(List<ProductImage> images) {
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
    }
}