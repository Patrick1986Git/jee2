package com.company.shop.module.product.exception;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

public class ProductReviewCountInvalidException extends BusinessException {

    public ProductReviewCountInvalidException(int reviewCount) {
        super(HttpStatus.CONFLICT,
                "Product review count cannot be negative: " + reviewCount,
                "PRODUCT_REVIEW_COUNT_INVALID");
    }
}
