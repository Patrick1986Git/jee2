/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.product.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.module.product.dto.ProductReviewRequestDTO;
import com.company.shop.module.product.dto.ProductReviewResponseDTO;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.product.entity.ProductReview;
import com.company.shop.module.product.repository.ProductRepository;
import com.company.shop.module.product.repository.ProductReviewRepository;
import com.company.shop.module.user.entity.User;
import com.company.shop.module.user.service.UserService;

import jakarta.persistence.EntityNotFoundException;

/**
 * Production implementation of {@link ProductReviewService} managing customer product reviews.
 * <p>
 * This service enforces business rules such as preventing duplicate reviews per user,
 * enforcing ownership-based deletion, and synchronizing product rating statistics
 * after each review modification within a single transaction.
 * </p>
 *
 * @since 1.0.0
 */
@Service
@Transactional
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository reviewRepo;
    private final ProductRepository productRepo;
    private final UserService userService;

    /**
     * Constructs the service with required dependencies.
     *
     * @param reviewRepo  repository for review persistence.
     * @param productRepo repository for product rating updates.
     * @param userService service for current user context.
     */
    public ProductReviewServiceImpl(ProductReviewRepository reviewRepo, ProductRepository productRepo, UserService userService) {
        this.reviewRepo = reviewRepo;
        this.productRepo = productRepo;
        this.userService = userService;
    }

    /**
     * Adds a new review for a product on behalf of the current user.
     * <p>
     * Enforces a one-review-per-user policy and recalculates product rating statistics
     * after the review is persisted.
     * </p>
     *
     * @param dto the review data including product identifier, rating, and comment.
     * @return the persisted review details.
     * @throws IllegalStateException   if the user has already reviewed this product.
     * @throws EntityNotFoundException if the specified product does not exist.
     */
    @Override
    public ProductReviewResponseDTO addReview(ProductReviewRequestDTO dto) {
        User user = userService.getCurrentUserEntity();

        if (reviewRepo.existsByProductIdAndUserId(dto.productId(), user.getId())) {
            throw new IllegalStateException("You have already reviewed this product.");
        }

        Product product = productRepo.findById(dto.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + dto.productId()));

        ProductReview review = new ProductReview(product, user, dto.rating(), dto.comment());
        ProductReview saved = reviewRepo.save(review);

        updateProductRatingStats(product);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductReviewResponseDTO> getProductReviews(UUID productId, Pageable pageable) {
        return reviewRepo.findByProductId(productId, pageable).map(this::mapToResponse);
    }

    /**
     * Deletes a review by its identifier.
     * <p>
     * Verifies that the current user is either the review author or an administrator.
     * Recalculates product rating statistics after deletion.
     * </p>
     *
     * @param reviewId the identifier of the review to delete.
     * @throws EntityNotFoundException if the review does not exist.
     * @throws AccessDeniedException   if the current user is not the author or an admin.
     */
    @Override
    public void deleteReview(UUID reviewId) {
        ProductReview review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with ID: " + reviewId));

        User currentUser = userService.getCurrentUserEntity();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isAdmin && !review.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this review.");
        }

        Product product = review.getProduct();
        reviewRepo.delete(review);

        updateProductRatingStats(product);
    }

    /**
     * Recalculates and persists the average rating and review count for a product.
     * <p>
     * Aggregates are fetched directly from the database to ensure consistency.
     * </p>
     *
     * @param product the product whose rating statistics should be updated.
     */
    private void updateProductRatingStats(Product product) {
        Double avg = reviewRepo.getAverageRatingForProduct(product.getId());
        long count = reviewRepo.countByProductIdAndDeletedFalse(product.getId());

        product.updateRatings(avg != null ? avg : 0.0, (int) count);
        productRepo.save(product);
    }

    private ProductReviewResponseDTO mapToResponse(ProductReview review) {
        return new ProductReviewResponseDTO(review.getId(),
                review.getUser().getFirstName() + " " + review.getUser().getLastName(), review.getRating(),
                review.getComment(), review.getCreatedAt());
    }
}