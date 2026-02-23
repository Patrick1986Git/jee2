/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.common.exception;

/**
 * Thrown when a user attempts to submit a second review for the same product.
 * <p>
 * Enforces the business rule of one review per user per product, providing
 * a clear, domain-specific exception instead of a generic conflict error.
 * </p>
 *
 * @since 1.0.0
 */
public class ReviewAlreadyExistsException extends RuntimeException {

    /**
     * Constructs the exception with a descriptive message.
     *
     * @param message a human-readable description of the constraint violation.
     */
    public ReviewAlreadyExistsException(String message) {
        super(message);
    }
}
