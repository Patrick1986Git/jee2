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

@Service
@Transactional
public class ProductReviewServiceImpl implements ProductReviewService {

	private final ProductReviewRepository reviewRepo;
	private final ProductRepository productRepo;
	private final UserService userService;

	public ProductReviewServiceImpl(ProductReviewRepository reviewRepo, ProductRepository productRepo, UserService userService) {
		this.reviewRepo = reviewRepo;
		this.productRepo = productRepo;
		this.userService = userService;
	}

	@Override
	public ProductReviewResponseDTO addReview(ProductReviewRequestDTO dto) {
	    User user = userService.getCurrentUserEntity();

	    if (reviewRepo.existsByProductIdAndUserId(dto.productId(), user.getId())) {
	        throw new IllegalStateException("You have already reviewed this product.");
	    }

	    Product product = productRepo.findById(dto.productId())
	            .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + dto.productId()));

	    // 1. Zapisz nową recenzję
	    ProductReview review = new ProductReview(product, user, dto.rating(), dto.comment());
	    ProductReview saved = reviewRepo.save(review);

	    // 2. Zaktualizuj statystyki produktu
	    updateProductRatingStats(product);

	    return mapToResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ProductReviewResponseDTO> getProductReviews(UUID productId, Pageable pageable) {
		return reviewRepo.findByProductId(productId, pageable).map(this::mapToResponse);
	}

	@Override
	public void deleteReview(UUID reviewId) {
	    ProductReview review = reviewRepo.findById(reviewId)
	            .orElseThrow(() -> new EntityNotFoundException("Review not found with ID: " + reviewId));

	    // ... (Twoja logika uprawnień) ...

	    Product product = review.getProduct();
	    reviewRepo.delete(review);

	    // 2. Po usunięciu również aktualizujemy statystyki
	    updateProductRatingStats(product);
	}

	/**
	 * Enterprise Grade approach: Przeliczanie statystyk.
	 * W skali miliona rekordów można to robić asynchronicznie, 
	 * ale przy starcie projektu @Transactional w zupełności wystarczy.
	 */
	private void updateProductRatingStats(Product product) {
	    // Pobieramy dane agregujące bezpośrednio z bazy dla spójności
	    // Możesz dodać dedykowaną metodę do ProductReviewRepository
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