package com.company.shop.module.product.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException(UUID productId) {
        super(HttpStatus.NOT_FOUND,
              "Product not found: " + productId,
              "PRODUCT_NOT_FOUND");
    }

    public ProductNotFoundException(String slug) {
        super(HttpStatus.NOT_FOUND,
              "Product not found for slug: " + slug,
              "PRODUCT_NOT_FOUND");
    }
}
