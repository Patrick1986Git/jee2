/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.product.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.annotations.SQLRestriction;

import com.company.shop.common.model.SoftDeleteEntity;
import com.company.shop.module.category.entity.Category;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Core entity representing a commercial product within the catalog.
 * <p>
 * This entity serves as an Aggregate Root for product-related data, including
 * inventory levels, pricing, and media assets. It maintains strict invariants
 * regarding stock management and financial calculations.
 * </p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "products")
@SQLRestriction("deleted = false")
public class Product extends SoftDeleteEntity {

	@Column(nullable = false, length = 255)
	private String name;

	@Column(nullable = false, unique = true, length = 255)
	private String slug;

	@Column(nullable = false, unique = true, length = 50)
	private String sku;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Column(nullable = false)
	private int stock;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id")
	private Category category;

	@Column(name = "average_rating", nullable = false, columnDefinition = "NUMERIC(3,2)")
	private Double averageRating = 0.0;

	@Column(name = "review_count", nullable = false)
	private int reviewCount = 0;

	/**
	 * Managed gallery of product images. Uses CascadeType.ALL and orphanRemoval to
	 * ensure that image lifecycle is strictly bound to the product.
	 */
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductImage> images = new ArrayList<>();

	protected Product() {
	}

	public Product(String name, String slug, String sku, String description, BigDecimal price, int stock,
			Category category) {
		this.name = name;
		this.slug = slug;
		this.sku = sku;
		this.description = description;
		this.price = price;
		this.stock = stock;
		this.category = category;
		this.averageRating = 0.0;
		this.reviewCount = 0;
	}

	/**
	 * Replaces the entire image gallery with a new set of URLs.
	 * <p>
	 * Due to {@code orphanRemoval}, existing images not present in the new list
	 * will be deleted from the database.
	 * </p>
	 *
	 * @param newImageUrls list of new image resource locations.
	 */
	public void replaceImages(List<String> newImageUrls) {
		this.images.clear();
		if (newImageUrls != null) {
			newImageUrls.forEach(this::addImage);
		}
	}

	/**
	 * Adds a single image to the product gallery.
	 *
	 * @param url the location of the image resource.
	 */
	public void addImage(String url) {
		ProductImage image = new ProductImage(url, this);
		this.images.add(image);
	}

	/**
	 * Resolves the primary image for the product.
	 *
	 * @return the URL of the first image in the gallery, or {@code null} if empty.
	 */
	public String getMainImageUrl() {
		if (images != null && !images.isEmpty()) {
			return images.get(0).getImageUrl();
		}
		return null;
	}

	public void updateRatings(Double newAverage, int newCount) {
		if (newCount < 0) {
			throw new IllegalArgumentException("Review count cannot be negative");
		}
		this.averageRating = newAverage;
		this.reviewCount = newCount;
	}

	public void update(String name, String slug, String description, BigDecimal price, int stock, Category category) {
		this.name = name;
		this.slug = slug;
		this.description = description;
		this.price = price;
		this.stock = stock;
		this.category = category;
	}

	public void updateStock(int newStock) {
		if (newStock < 0) {
			throw new IllegalArgumentException("Stock level cannot be negative");
		}
		this.stock = newStock;
	}

	/**
	 * Safely decreases stock level.
	 *
	 * @param quantityToDecrease number of units to remove.
	 * @throws IllegalStateException if the requested quantity exceeds available
	 *                               stock.
	 */
	public void decreaseStock(int quantityToDecrease) {
		if (quantityToDecrease <= 0) {
			throw new IllegalArgumentException("Quantity to decrease must be positive");
		}
		if (this.stock < quantityToDecrease) {
			throw new IllegalStateException(
					"Insufficient stock for product: " + this.name + ". Available: " + this.stock);
		}
		this.stock -= quantityToDecrease;
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

	public Category getCategory() {
		return category;
	}

	public Double getAverageRating() {
		return averageRating;
	}

	public int getReviewCount() {
		return reviewCount;
	}

	/**
	 * Returns an unmodifiable view of the product images.
	 *
	 * @return a read-only list of {@link ProductImage} entities.
	 */
	public List<ProductImage> getImages() {
		return Collections.unmodifiableList(images);
	}
}