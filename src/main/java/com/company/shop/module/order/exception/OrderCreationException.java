package com.company.shop.module.order.exception;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

/**
 * Exception thrown when order creation process fails unexpectedly.
 */
public class OrderCreationException extends BusinessException {

    public OrderCreationException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, "ORDER_CREATION_ERROR");
    }
}
