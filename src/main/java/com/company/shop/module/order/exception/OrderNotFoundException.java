package com.company.shop.module.order.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

/**
 * Exception thrown when an order cannot be found by its identifier.
 */
public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException(UUID orderId) {
        super(HttpStatus.NOT_FOUND, "Order not found: " + orderId, "ORDER_NOT_FOUND");
    }
}
