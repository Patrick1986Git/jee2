/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for detailed product information.
 * <p>
 * This class provides a read-only snapshot of a product's state, enriched with 
 * category details and media assets. It is designed to be immutable to ensure 
 * thread-safety and data integrity across application layers.
 * </p>
 *
 * @since 1.0.0
 */
public class ProductResponseDTO {

    private final UUID id;
    private final String name;
    private final String slug;
    private final String sku;
    private final String description;
    private final BigDecimal price;
    private final int stock;
    private final UUID categoryId;
    private final String categoryName;
    private final Double averageRating;
    private final int reviewCount;
    
    /**
     * List of associated image URLs, typically ordered by display priority.
     */
    private final List<String> imageUrls;

    /**
     * Full constructor for initializing an immutable product response.
     *
     * @param id            unique product identifier.
     * @param name          display name.
     * @param slug          SEO-friendly URL identifier.
     * @param sku           stock keeping unit.
     * @param description   marketing content.
     * @param price         current unit price.
     * @param stock         available quantity.
     * @param categoryId    parent category ID.
     * @param categoryName  parent category display name (flattened).
     * @param averageRating computed user rating (defaults to 0.0 if null).
     * @param reviewCount   total number of submitted reviews.
     * @param imageUrls     collection of image resource locations.
     */
    public ProductResponseDTO(UUID id, String name, String slug, String sku, String description, BigDecimal price,
            int stock, UUID categoryId, String categoryName, Double averageRating, int reviewCount,
            List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.sku = sku;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.reviewCount = reviewCount;
        this.imageUrls = imageUrls;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getSku() {
        return sku;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    /**
     * Retrieves the list of URLs for product images.
     *
     * @return an unmodifiable list of image URLs.
     */
    public List<String> getImageUrls() {
        return imageUrls;
    }
}