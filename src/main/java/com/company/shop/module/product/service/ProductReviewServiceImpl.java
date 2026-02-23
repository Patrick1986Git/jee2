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

import com.company.shop.common.exception.ReviewAlreadyExistsException;
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
 * Production implementation of {@link ProductReviewService} handling product review lifecycle.
 * <p>
 * Enforces the business rule of one review per user per product, maintains aggregate 
 * rating statistics on the product entity, and provides paginated review retrieval.
 * </p>
 *
 * @since 1.0.0
 */
@Service
@Transactional
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    /**
     * Constructs the service with required dependencies.
     *
     * @param reviewRepository  repository for review persistence.
     * @param productRepository repository for product rating updates.
     * @param userService       service for current user context.
     */
    public ProductReviewServiceImpl(ProductReviewRepository reviewRepository,
                                    ProductRepository productRepository,
                                    UserService userService) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    /**
     * Submits a new review for a product.
     * <p>
     * Validates that the user has not already reviewed the product, then persists
     * the review and recalculates the product's aggregate rating statistics.
     * </p>
     *
     * @param dto the review data containing product ID, rating, and comment.
     * @return the persisted review details.
     * @throws ReviewAlreadyExistsException if the user has already reviewed the product.
     * @throws EntityNotFoundException      if the specified product does not exist.
     */
    @Override
    public ProductReviewResponseDTO addReview(ProductReviewRequestDTO dto) {
        User user = userService.getCurrentUserEntity();

        if (reviewRepository.existsByProductIdAndUserId(dto.productId(), user.getId())) {
            throw new ReviewAlreadyExistsException("You have already submitted a review for this product.");
        }

        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + dto.productId()));

        ProductReview review = new ProductReview(product, user, dto.rating(), dto.comment());
        ProductReview saved = reviewRepository.save(review);

        updateProductRatingStats(product);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductReviewResponseDTO> getProductReviews(UUID productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable).map(this::mapToResponse);
    }

    /**
     * Deletes a review and recalculates the product's aggregate rating statistics.
     *
     * @param reviewId the identifier of the review to delete.
     * @throws EntityNotFoundException if the review does not exist.
     * @throws AccessDeniedException   if the current user is not authorized to delete the review.
     */
    @Override
    public void deleteReview(UUID reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with ID: " + reviewId));

        User currentUser = userService.getCurrentUserEntity();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isAdmin && !review.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this review.");
        }

        Product product = review.getProduct();
        reviewRepository.delete(review);

        updateProductRatingStats(product);
    }

    /**
     * Recalculates and persists aggregate rating statistics for a product.
     * <p>
     * Queries the database directly for consistency. For very high-volume scenarios
     * this operation can be moved to an asynchronous event handler.
     * </p>
     *
     * @param product the product whose statistics must be updated.
     */
    private void updateProductRatingStats(Product product) {
        Double avg = reviewRepository.getAverageRatingForProduct(product.getId());
        long count = reviewRepository.countByProductIdAndDeletedFalse(product.getId());
        product.updateRatings(avg != null ? avg : 0.0, (int) count);
        productRepository.save(product);
    }

    private ProductReviewResponseDTO mapToResponse(ProductReview review) {
        return new ProductReviewResponseDTO(
                review.getId(),
                review.getUser().getFirstName() + " " + review.getUser().getLastName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
