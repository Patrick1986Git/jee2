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
import com.company.shop.module.product.exception.ProductInsufficientStockException;
import com.company.shop.module.product.exception.ProductReviewCountInvalidException;
import com.company.shop.module.product.exception.ProductStockInvalidException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

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

	@Version
	@Column(name = "version", nullable = false)
	private long version;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id")
	private Category category;

	@Column(name = "average_rating", nullable = false, columnDefinition = "NUMERIC(3,2)")
	private Double averageRating = 0.0;

	@Column(name = "review_count", nullable = false)
	private int reviewCount = 0;

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

	public void replaceImages(List<String> newImageUrls) {
		this.images.clear();
		if (newImageUrls != null) {
			newImageUrls.forEach(this::addImage);
		}
	}

	public void addImage(String url) {
		ProductImage image = new ProductImage(url, this);
		this.images.add(image);
	}

	public String getMainImageUrl() {
		if (images != null && !images.isEmpty()) {
			return images.get(0).getImageUrl();
		}
		return null;
	}

	public void updateRatings(Double newAverage, int newCount) {
		if (newCount < 0) {
			throw new ProductReviewCountInvalidException(newCount);
		}
		this.averageRating = newAverage;
		this.reviewCount = newCount;
	}

	public void update(String name, String slug, String sku, String description, BigDecimal price, int stock, Category category) {
		this.name = name;
		this.slug = slug;
		this.sku = sku;
		this.description = description;
		this.price = price;
		this.stock = stock;
		this.category = category;
	}

	public void updateStock(int newStock) {
		if (newStock < 0) {
			throw new ProductStockInvalidException(newStock);
		}
		this.stock = newStock;
	}

	public void decreaseStock(int quantityToDecrease) {
		if (quantityToDecrease <= 0) {
			throw new ProductStockInvalidException("Quantity to decrease must be greater than zero");
		}
		if (this.stock < quantityToDecrease) {
			throw new ProductInsufficientStockException(this.name, quantityToDecrease, this.stock);
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

	public List<ProductImage> getImages() {
		return Collections.unmodifiableList(images);
	}
}
