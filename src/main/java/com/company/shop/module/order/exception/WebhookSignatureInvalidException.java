package com.company.shop.module.order.exception;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

/**
 * Exception thrown when Stripe webhook signature or payload is invalid.
 */
public class WebhookSignatureInvalidException extends BusinessException {

    public WebhookSignatureInvalidException() {
        super(HttpStatus.BAD_REQUEST, "Invalid Stripe webhook signature or payload.", "STRIPE_WEBHOOK_SIGNATURE_INVALID");
    }
}
