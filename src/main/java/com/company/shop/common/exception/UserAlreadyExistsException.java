/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.common.exception;

/**
 * Thrown when a registration attempt is made with an email address already in use.
 * <p>
 * This exception prevents duplicate account creation and provides a meaningful
 * error message to guide the user toward recovery (e.g., login or password reset).
 * </p>
 *
 * @since 1.0.0
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Constructs the exception with a message indicating the conflicting email.
     *
     * @param message a human-readable description of the conflict.
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
