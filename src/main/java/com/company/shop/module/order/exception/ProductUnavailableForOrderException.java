package com.company.shop.module.order.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

/**
 * Exception thrown when a product cannot be used during checkout.
 */
public class ProductUnavailableForOrderException extends BusinessException {

    public ProductUnavailableForOrderException(UUID productId) {
        super(HttpStatus.NOT_FOUND, "Product not found for checkout: " + productId, "ORDER_PRODUCT_NOT_FOUND");
    }

    public ProductUnavailableForOrderException(UUID productId, int requestedQuantity, int availableQuantity) {
        super(
                HttpStatus.CONFLICT,
                "Insufficient stock for product " + productId + ". Requested: " + requestedQuantity + ", available: "
                        + availableQuantity,
                "ORDER_INSUFFICIENT_STOCK");
    }
}
