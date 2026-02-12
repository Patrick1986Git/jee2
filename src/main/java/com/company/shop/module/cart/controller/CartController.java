/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.cart.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.cart.dto.AddToCartRequestDTO;
import com.company.shop.module.cart.dto.CartResponseDTO;
import com.company.shop.module.cart.dto.UpdateCartItemRequestDTO;
import com.company.shop.module.cart.service.CartService;

import jakarta.validation.Valid;

/**
 * REST controller for managing the shopping cart operations.
 * <p>
 * Provides endpoints for retrieving, adding, updating, and clearing the authenticated
 * user's shopping cart. Access is restricted to authenticated users only.
 * </p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/cart")
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;

    /**
     * Constructs the controller with the cart service.
     *
     * @param cartService service handling business logic for cart operations.
     */
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Retrieves the shopping cart of the current user.
     *
     * @return {@link ResponseEntity} containing the {@link CartResponseDTO}.
     */
    @GetMapping
    public ResponseEntity<CartResponseDTO> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    /**
     * Adds a specific product to the user's shopping cart.
     *
     * @param request DTO containing product ID and initial quantity.
     * @return {@link ResponseEntity} with the updated cart state.
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItemToCart(@Valid @RequestBody AddToCartRequestDTO request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    /**
     * Updates the quantity of an existing item in the cart.
     * <p>
     * Uses {@code PATCH} as it performs a partial update of the cart resource.
     * </p>
     *
     * @param productId unique identifier of the product to update.
     * @param request   DTO containing the new absolute quantity.
     * @return {@link ResponseEntity} with the updated cart state.
     */
    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartResponseDTO> updateItemQuantity(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateCartItemRequestDTO request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(productId, request));
    }

    /**
     * Removes a product entirely from the user's shopping cart.
     *
     * @param productId unique identifier of the product to remove.
     * @return {@link ResponseEntity} with the cart state after removal.
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponseDTO> removeItemFromCart(@PathVariable UUID productId) {
        return ResponseEntity.ok(cartService.removeItem(productId));
    }

    /**
     * Clears all items from the current user's cart.
     *
     * @return {@link ResponseEntity} with 204 No Content status on success.
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}