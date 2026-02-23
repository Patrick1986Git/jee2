/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.common.exception;

/**
 * Thrown when an attempt is made to create a resource that already exists.
 * <p>
 * Common use cases include duplicate product SKUs, duplicate category names,
 * and other unique-constraint violations at the business logic layer.
 * </p>
 *
 * @since 1.0.0
 */
public class DuplicateResourceException extends RuntimeException {

    /**
     * Constructs the exception with a message identifying the conflicting resource.
     *
     * @param message a human-readable description of the duplicate resource.
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
