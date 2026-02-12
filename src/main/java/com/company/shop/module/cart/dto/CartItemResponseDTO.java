/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data Transfer Object representing a single line item in the shopping cart.
 * <p>
 * This DTO provides comprehensive information about a product in the cart, 
 * including pricing calculations and current stock status to inform the user
 * about product availability.
 * </p>
 *
 * @param productId      Unique identifier of the product.
 * @param productName    Display name of the product.
 * @param productSlug    SEO-friendly URL identifier.
 * @param mainImageUrl   Primary image resource location for the product thumbnail.
 * @param unitPrice      Current price of a single unit of the product.
 * @param quantity       The number of units present in the cart.
 * @param subtotal       Calculated total for this item (unitPrice * quantity).
 * @param stockAvailable Current quantity available in the warehouse.
 * @param isLowStock     Flag indicating if the stock level is below the business threshold.
 * * @since 1.0.0
 */
public record CartItemResponseDTO(
        UUID productId,
        String productName,
        String productSlug,
        String mainImageUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal,
        int stockAvailable,
        boolean isLowStock
) {}