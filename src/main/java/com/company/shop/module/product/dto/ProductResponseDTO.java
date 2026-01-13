package com.company.shop.module.product.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductResponseDTO {

	private final UUID id;
	private final String name;
	private final String slug;
	private final String sku;
	private final String description;
	private final BigDecimal price;
	private final int stock;

	private final UUID categoryId; // Dane kategorii spłaszczone do ID i nazwy (Flattening)
	private final String categoryName;

	public ProductResponseDTO(UUID id, String name, String slug, String sku, String description, BigDecimal price,
			int stock, UUID categoryId, String categoryName) {
		this.id = id;
		this.name = name;
		this.slug = slug;
		this.sku = sku;
		this.description = description;
		this.price = price;
		this.stock = stock;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
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
}