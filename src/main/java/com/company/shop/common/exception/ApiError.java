/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.common.exception;

import java.time.LocalDateTime;

/**
 * Standardized API error response container.
 * <p>
 * This record ensures that all error responses across the system maintain 
 * a consistent structure, facilitating easier error handling for client applications.
 * </p>
 *
 * @param status    the HTTP status code value.
 * @param message   a human-readable description of the error.
 * @param errors    optional detailed error information (e.g., field-level validation errors).
 * @param timestamp the exact time the error occurred in the server's local time.
 * @since 1.0.0
 */
public record ApiError(int status, String message, Object errors, LocalDateTime timestamp) {

    /**
     * Compact constructor for general application errors (e.g., 404 Not Found, 500 Internal Error).
     *
     * @param status  the HTTP status code.
     * @param message description of the error.
     */
    public ApiError(int status, String message) {
        this(status, message, null, LocalDateTime.now());
    }

    /**
     * Specialized constructor for validation-related errors (e.g., 400 Bad Request).
     *
     * @param status  the HTTP status code.
     * @param message overview of the validation failure.
     * @param errors  collection of field-specific error messages.
     */
    public ApiError(int status, String message, Object errors) {
        this(status, message, errors, LocalDateTime.now());
    }
}