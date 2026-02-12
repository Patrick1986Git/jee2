/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.cart.service;

import java.util.UUID;

import com.company.shop.module.cart.dto.AddToCartRequestDTO;
import com.company.shop.module.cart.dto.CartResponseDTO;
import com.company.shop.module.cart.dto.UpdateCartItemRequestDTO;

/**
 * Service interface for managing shopping cart operations.
 * <p>
 * This service handles business logic for user-specific carts, including 
 * item persistence, quantity management, and cart lifecycle. All operations 
 * are performed within the context of the currently authenticated user.
 * </p>
 *
 * @since 1.0.0
 */
public interface CartService {

    /**
     * Retrieves the shopping cart for the currently authenticated user.
     * <p>
     * If a cart does not exist for the user, a new one may be initialized 
     * depending on the implementation.
     * </p>
     *
     * @return a {@link CartResponseDTO} containing all cart items and totals.
     */
    CartResponseDTO getMyCart();

    /**
     * Adds a product to the user's cart.
     * <p>
     * If the product is already present in the cart, the quantity will be increased.
     * Validates product existence and stock availability before addition.
     * </p>
     *
     * @param request DTO containing product identifier and quantity.
     * @return the updated {@link CartResponseDTO}.
     */
    CartResponseDTO addToCart(AddToCartRequestDTO request);

    /**
     * Updates the quantity of an existing item in the cart.
     * <p>
     * Performs boundary checks to ensure the requested quantity is valid 
     * and available in stock.
     * </p>
     *
     * @param productId unique identifier of the product in the cart.
     * @param request   DTO containing the new absolute quantity.
     * @return the updated {@link CartResponseDTO}.
     */
    CartResponseDTO updateItemQuantity(UUID productId, UpdateCartItemRequestDTO request);

    /**
     * Removes a specific product from the user's cart regardless of its quantity.
     *
     * @param productId unique identifier of the product to remove.
     * @return the updated {@link CartResponseDTO} after removal.
     */
    CartResponseDTO removeItem(UUID productId);

    /**
     * Clears all items from the current user's cart.
     * <p>
     * This operation is typically performed after a successful order placement 
     * or upon explicit user request.
     * </p>
     */
    void clearCart();
}