/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.common.exception;

/**
 * Thrown when communication with the external payment gateway fails.
 * <p>
 * Wraps low-level Stripe API exceptions and webhook processing errors into a 
 * domain-specific exception, decoupling the rest of the application from 
 * the payment provider's SDK.
 * </p>
 *
 * @since 1.0.0
 */
public class PaymentProcessingException extends RuntimeException {

    /**
     * Constructs the exception with a message and the underlying cause.
     *
     * @param message a human-readable description of the payment failure.
     * @param cause   the original exception thrown by the payment provider.
     */
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
