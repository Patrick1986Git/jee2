/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.cart.mapper;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.company.shop.module.cart.dto.CartItemResponseDTO;
import com.company.shop.module.cart.dto.CartResponseDTO;
import com.company.shop.module.cart.entity.Cart;
import com.company.shop.module.cart.entity.CartItem;

/**
 * Mapper component for converting Cart-related entities to Data Transfer Objects.
 * <p>
 * This mapper leverages MapStruct to handle complex object graphs and perform 
 * real-time business calculations like subtotals and stock availability checks 
 * during the transformation process.
 * </p>
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface CartMapper {

    /**
     * Threshold defining the "Low Stock" status for product availability.
     */
    int LOW_STOCK_THRESHOLD = 5;

    /**
     * Transforms a {@link Cart} aggregate root into a comprehensive {@link CartResponseDTO}.
     *
     * @param cart the source entity containing cart items and user information.
     * @return a flattened DTO with calculated totals and item details.
     */
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalAmount", expression = "java(cart.calculateTotalAmount())")
    @Mapping(target = "totalItemsCount", expression = "java(cart.getItems().stream().mapToInt(com.company.shop.module.cart.entity.CartItem::getQuantity).sum())")
    CartResponseDTO toDTO(Cart cart);

    /**
     * Maps a {@link CartItem} entity to its corresponding {@link CartItemResponseDTO}.
     * <p>
     * Performs mapping from nested Product attributes and invokes custom logic for 
     * financial and inventory status calculations.
     * </p>
     *
     * @param item the source line item from the cart.
     * @return a DTO enriched with product details and calculated subtotals.
     */
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "unitPrice", source = "product.price")
    @Mapping(target = "stockAvailable", source = "product.stock")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(item))")
    @Mapping(target = "isLowStock", source = "item", qualifiedByName = "checkLowStock")
    CartItemResponseDTO toItemDTO(CartItem item);

    /**
     * Custom calculation logic for line item subtotal.
     *
     * @param item the cart item to calculate for.
     * @return unit price multiplied by quantity, or {@link BigDecimal#ZERO} if null.
     */
    @Named("calculateSubtotal")
    default BigDecimal calculateSubtotal(CartItem item) {
        if (item == null || item.getProduct() == null)
            return BigDecimal.ZERO;
        return item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    /**
     * Business rule for evaluating low stock alerts.
     *
     * @param item the cart item to check.
     * @return true if product stock is below {@link #LOW_STOCK_THRESHOLD}.
     */
    @Named("checkLowStock")
    default boolean checkLowStock(CartItem item) {
        if (item == null || item.getProduct() == null)
            return false;
        return item.getProduct().getStock() < LOW_STOCK_THRESHOLD;
    }
}