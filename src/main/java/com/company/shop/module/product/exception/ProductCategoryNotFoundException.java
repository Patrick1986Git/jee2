package com.company.shop.module.product.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

public class ProductCategoryNotFoundException extends BusinessException {

    public ProductCategoryNotFoundException(UUID categoryId) {
        super(HttpStatus.NOT_FOUND,
                "Category not found for product operation: " + categoryId,
                "PRODUCT_CATEGORY_NOT_FOUND");
    }
}
