package com.company.shop.module.product.exception;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

public class ProductStockInvalidException extends BusinessException {

    public ProductStockInvalidException(int stock) {
        super(HttpStatus.CONFLICT,
                "Product stock cannot be negative: " + stock,
                "PRODUCT_STOCK_INVALID");
    }

    public ProductStockInvalidException(String message) {
        super(HttpStatus.CONFLICT,
                message,
                "PRODUCT_STOCK_INVALID");
    }

    public ProductStockInvalidException(String operation, int quantity) {
        super(HttpStatus.CONFLICT,
                "Invalid stock operation '" + operation + "' for quantity: " + quantity,
                "PRODUCT_STOCK_INVALID");
    }
}
