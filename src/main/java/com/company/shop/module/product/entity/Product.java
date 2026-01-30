package com.company.shop.module.product.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.SQLRestriction;

import com.company.shop.common.model.SoftDeleteEntity;
import com.company.shop.module.category.entity.Category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

	protected Product() {
		// JPA
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
	 * Logika biznesowa aktualizacji statystyk recenzji. Metoda ta powinna być
	 * wywoływana przez serwis po dodaniu/usunięciu recenzji.
	 */
	public void updateRatings(Double newAverage, int newCount) {
		if (newCount < 0) {
			throw new IllegalArgumentException("Liczba recenzji nie może być ujemna");
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
			throw new IllegalArgumentException("Stan magazynowy nie może być ujemny");
		}
		this.stock = newStock;
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
}