/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.common.exception;

/**
 * Thrown when a requested operation cannot be completed due to insufficient product stock.
 * <p>
 * This exception is raised during cart item additions or order placements when the
 * requested quantity exceeds available warehouse inventory.
 * </p>
 *
 * @since 1.0.0
 */
public class InsufficientStockException extends RuntimeException {

    /**
     * Constructs the exception with a descriptive message indicating available vs. requested stock.
     *
     * @param message a human-readable description of the stock shortage.
     */
    public InsufficientStockException(String message) {
        super(message);
    }
}
