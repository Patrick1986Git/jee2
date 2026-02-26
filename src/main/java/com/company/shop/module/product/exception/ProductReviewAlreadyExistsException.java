package com.company.shop.module.product.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

public class ProductReviewAlreadyExistsException extends BusinessException {

    public ProductReviewAlreadyExistsException(UUID productId) {
        super(HttpStatus.CONFLICT,
                "Review already exists for product: " + productId,
                "PRODUCT_REVIEW_ALREADY_EXISTS");
    }
}
