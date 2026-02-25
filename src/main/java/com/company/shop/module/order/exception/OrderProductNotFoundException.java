package com.company.shop.module.order.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

/**
 * Exception thrown when a product referenced by cart item does not exist during checkout.
 */
public class OrderProductNotFoundException extends BusinessException {

    public OrderProductNotFoundException(UUID productId) {
        super(HttpStatus.NOT_FOUND, "Product not found for checkout: " + productId, "ORDER_PRODUCT_NOT_FOUND");
    }
}
