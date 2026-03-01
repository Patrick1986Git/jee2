package com.company.shop.module.product.entity;

import org.hibernate.annotations.SQLRestriction;

import com.company.shop.common.model.SoftDeleteEntity;
import com.company.shop.module.product.exception.ProductReviewRatingInvalidException;
import com.company.shop.module.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "product_reviews")
@SQLRestriction("deleted = false")
public class ProductReview extends SoftDeleteEntity {

	private static final int MIN_RATING = 1;
	private static final int MAX_RATING = 5;

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

	@Version
	@Column(name = "version", nullable = false)
	private long version;

	protected ProductReview() {
	}

	public ProductReview(Product product, User user, int rating, String comment) {
		validateRating(rating);
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
		validateRating(rating);
		this.rating = rating;
		this.comment = comment;
	}

	private void validateRating(int rating) {
		if (rating < MIN_RATING || rating > MAX_RATING) {
			throw new ProductReviewRatingInvalidException(rating);
		}
	}
}
