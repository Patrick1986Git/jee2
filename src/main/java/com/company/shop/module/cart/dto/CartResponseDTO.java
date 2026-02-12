/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.cart.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object representing the complete state of a shopping cart.
 * <p>
 * This DTO aggregates all line items and provides summary calculations 
 * such as total value and total item count. It serves as the primary 
 * response object for cart-related operations.
 * </p>
 *
 * @param id               Unique identifier of the cart.
 * @param items            List of detailed item information within the cart.
 * @param totalAmount      Sum of all item subtotals (gross total).
 * @param totalItemsCount  Aggregate count of all product units in the cart.
 * @since 1.0.0
 */
public record CartResponseDTO(
        UUID id,
        List<CartItemResponseDTO> items,
        BigDecimal totalAmount,
        int totalItemsCount
) {}