package com.company.shop.module.product.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.shop.module.product.entity.ProductReview;

public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {

	Page<ProductReview> findByProductId(UUID productId, Pageable pageable);

	boolean existsByProductIdAndUserId(UUID productId, UUID userId);

	Optional<ProductReview> findByProductIdAndUserId(UUID productId, UUID userId);

	@Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId AND r.deleted = false")
	Double getAverageRatingForProduct(@Param("productId") UUID productId);

	@Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.deleted = false")
	long countByProductIdAndDeletedFalse(@Param("productId") UUID productId);
}