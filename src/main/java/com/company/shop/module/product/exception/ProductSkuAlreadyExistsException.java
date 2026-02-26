package com.company.shop.module.product.exception;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

public class ProductSkuAlreadyExistsException extends BusinessException {

    public ProductSkuAlreadyExistsException(String sku) {
        super(HttpStatus.CONFLICT,
                "Product already exists with SKU: " + sku,
                "PRODUCT_SKU_ALREADY_EXISTS");
    }
}
