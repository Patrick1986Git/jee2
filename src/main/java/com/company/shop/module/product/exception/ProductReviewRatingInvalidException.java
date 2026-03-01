package com.company.shop.module.product.exception;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

public class ProductReviewRatingInvalidException extends BusinessException {

    public ProductReviewRatingInvalidException(int rating) {
        super(HttpStatus.BAD_REQUEST,
                "Product review rating must be between 1 and 5. Provided: " + rating,
                "PRODUCT_REVIEW_RATING_INVALID");
    }
}
