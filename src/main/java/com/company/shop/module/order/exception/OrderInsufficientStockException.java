package com.company.shop.module.order.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

/**
 * Exception thrown when available stock is lower than requested quantity during checkout.
 */
public class OrderInsufficientStockException extends BusinessException {

    public OrderInsufficientStockException(UUID productId, int requestedQuantity, int availableQuantity) {
        super(HttpStatus.CONFLICT,
                "Insufficient stock for product " + productId + ". Requested: " + requestedQuantity + ", available: "
                        + availableQuantity,
                "ORDER_INSUFFICIENT_STOCK");
    }
}
