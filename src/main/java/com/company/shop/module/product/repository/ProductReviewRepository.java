package com.company.shop.module.product.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.company.shop.module.product.entity.ProductReview;

public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
	Page<ProductReview> findByProductId(UUID productId, Pageable pageable);

	boolean existsByProductIdAndUserId(UUID productId, UUID userId);

	Optional<ProductReview> findByProductIdAndUserId(UUID productId, UUID userId);
}