/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.common.exception;

/**
 * Thrown when a discount code provided during checkout is invalid or expired.
 * <p>
 * This exception distinguishes discount code validation failures from generic
 * bad request errors, allowing for targeted client-side handling and clear
 * API documentation.
 * </p>
 *
 * @since 1.0.0
 */
public class InvalidDiscountCodeException extends RuntimeException {

    /**
     * Constructs the exception with a message indicating the invalid code.
     *
     * @param message a human-readable description of the validation failure.
     */
    public InvalidDiscountCodeException(String message) {
        super(message);
    }
}
