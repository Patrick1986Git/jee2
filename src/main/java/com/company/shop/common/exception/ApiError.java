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
 * Standardized API error response container for production-grade applications.
 * <p>
 * This record ensures that all error responses across the system maintain 
 * a consistent structure. The addition of {@code errorCode} allows client 
 * applications to perform programmatic logic based on specific error types 
 * rather than parsing human-readable messages.
 * </p>
 *
 * @param status    the HTTP status code value (e.g., 400, 404, 500).
 * @param message   a human-readable description of the error (may be localized).
 * @param errorCode a unique, machine-readable string identifying the specific error (e.g., "USER_NOT_FOUND").
 * @param errors    optional detailed information (e.g., field-level validation errors).
 * @param timestamp the exact time the error occurred.
 * @since 1.1.0
 */
public record ApiError(
        int status,
        String message,
        String errorCode,
        Object errors,
        LocalDateTime timestamp
) {

    public ApiError(int status, String message) {
        this(status, message, null, null, LocalDateTime.now());
    }

    public ApiError(int status, String message, String errorCode) {
        this(status, message, errorCode, null, LocalDateTime.now());
    }

    public ApiError(int status, String message, String errorCode, Object errors) {
        this(status, message, errorCode, errors, LocalDateTime.now());
    }

    public ApiError(int status, String message, Object errors) {
        this(status, message, "VALIDATION_FAILED", errors, LocalDateTime.now());
    }
}