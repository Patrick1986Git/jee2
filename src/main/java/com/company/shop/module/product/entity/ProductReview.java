package com.company.shop.module.product.entity;

import org.hibernate.annotations.SQLRestriction;

import com.company.shop.common.model.SoftDeleteEntity;
import com.company.shop.module.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_reviews")
@SQLRestriction("deleted = false")
public class ProductReview extends SoftDeleteEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id")
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false)
	private int rating;

	@Column(columnDefinition = "TEXT")
	private String comment;

	protected ProductReview() {
	}

	public ProductReview(Product product, User user, int rating, String comment) {
		this.product = product;
		this.user = user;
		this.rating = rating;
		this.comment = comment;
	}

	public Product getProduct() {
		return product;
	}

	public User getUser() {
		return user;
	}

	public int getRating() {
		return rating;
	}

	public String getComment() {
		return comment;
	}

	public void update(int rating, String comment) {
		this.rating = rating;
		this.comment = comment;
	}
}