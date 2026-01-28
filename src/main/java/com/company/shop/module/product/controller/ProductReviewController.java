package com.company.shop.module.product.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.product.dto.ProductReviewRequestDTO;
import com.company.shop.module.product.dto.ProductReviewResponseDTO;
import com.company.shop.module.product.service.ProductReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/products/reviews")
public class ProductReviewController {

	private final ProductReviewService reviewService;

	public ProductReviewController(ProductReviewService reviewService) {
		this.reviewService = reviewService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("isAuthenticated()")
	public ProductReviewResponseDTO addReview(@Valid @RequestBody ProductReviewRequestDTO dto) {
		return reviewService.addReview(dto);
	}

	@GetMapping("/{productId}")
	public Page<ProductReviewResponseDTO> getReviews(@PathVariable UUID productId, Pageable pageable) {
		return reviewService.getProductReviews(productId, pageable);
	}

	@DeleteMapping("/{reviewId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("isAuthenticated()")
	public void deleteReview(@PathVariable UUID reviewId) {
		reviewService.deleteReview(reviewId);
	}
}