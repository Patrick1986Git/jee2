package com.company.shop.module.product.exception;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

public class ProductSlugAlreadyExistsException extends BusinessException {

    public ProductSlugAlreadyExistsException(String slug) {
        super(HttpStatus.CONFLICT,
                "Product already exists with slug: " + slug,
                "PRODUCT_SLUG_ALREADY_EXISTS");
    }
}
