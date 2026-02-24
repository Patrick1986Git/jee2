package com.company.shop.module.order.exception;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

/**
 * Exception thrown when Stripe webhook processing fails.
 */
public class WebhookProcessingException extends BusinessException {

    public WebhookProcessingException(String message) {
        super(HttpStatus.BAD_REQUEST, message, "STRIPE_WEBHOOK_ERROR");
    }
}
