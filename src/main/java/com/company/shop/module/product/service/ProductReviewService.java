package com.company.shop.module.product.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.company.shop.module.product.dto.ProductReviewRequestDTO;
import com.company.shop.module.product.dto.ProductReviewResponseDTO;

public interface ProductReviewService {
	ProductReviewResponseDTO addReview(ProductReviewRequestDTO dto);

	Page<ProductReviewResponseDTO> getProductReviews(UUID productId, Pageable pageable);

	void deleteReview(UUID reviewId);
}