/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.common.exception;

/**
 * Thrown when an order placement is attempted on an empty shopping cart.
 * <p>
 * This exception enforces the business rule that an order must contain at least
 * one item before it can be submitted for processing.
 * </p>
 *
 * @since 1.0.0
 */
public class EmptyCartException extends RuntimeException {

    /**
     * Constructs the exception with a descriptive message.
     *
     * @param message a human-readable description of the error condition.
     */
    public EmptyCartException(String message) {
        super(message);
    }
}
