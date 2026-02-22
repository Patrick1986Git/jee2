/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.cart.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.common.exception.CartNotFoundException;
import com.company.shop.common.exception.InsufficientStockException;
import com.company.shop.common.exception.ProductNotFoundException;
import com.company.shop.module.cart.dto.AddToCartRequestDTO;
import com.company.shop.module.cart.dto.CartResponseDTO;
import com.company.shop.module.cart.dto.UpdateCartItemRequestDTO;
import com.company.shop.module.cart.entity.Cart;
import com.company.shop.module.cart.entity.CartItem;
import com.company.shop.module.cart.mapper.CartMapper;
import com.company.shop.module.cart.repository.CartRepository;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.product.repository.ProductRepository;
import com.company.shop.module.user.entity.User;
import com.company.shop.module.user.service.UserService;

/**
 * Production implementation of {@link CartService} providing shopping cart management.
 * <p>
 * This service handles transactional operations for cart lifecycle, including 
 * stock availability validation and automated cart initialization for new users.
 * All operations are bound to the security context provided by {@link UserService}.
 * </p>
 *
 * @since 1.0.0
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final CartMapper cartMapper;

    /**
     * Constructs the service with required dependencies.
     *
     * @param cartRepository    repository for cart persistence.
     * @param productRepository repository for product stock validation.
     * @param userService       service for current user context.
     * @param cartMapper        mapper for DTO transformation.
     */
    public CartServiceImpl(CartRepository cartRepository, 
                           ProductRepository productRepository, 
                           UserService userService, 
                           CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userService = userService;
        this.cartMapper = cartMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getMyCart() {
        User user = userService.getCurrentUserEntity();
        Cart cart = getOrCreateCart(user);
        return cartMapper.toDTO(cart);
    }

    /**
     * Adds a product to the cart with strict stock checking.
     *
     * @param request DTO containing product identifier and quantity.
     * @return updated {@link CartResponseDTO}.
     * @throws ProductNotFoundException if the requested product does not exist.
     * @throws InsufficientStockException if the combined quantity exceeds warehouse stock.
     */
    @Override
    public CartResponseDTO addToCart(AddToCartRequestDTO request) {
        User user = userService.getCurrentUserEntity();
        Cart cart = getOrCreateCart(user);
        
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.productId()));

        int currentInCart = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.productId()))
                .mapToInt(CartItem::getQuantity)
                .sum();

        if (product.getStock() < (currentInCart + request.quantity())) {
            throw new InsufficientStockException("Insufficient stock. Available: " + product.getStock() + ", in cart: " + currentInCart);
        }

        cart.addItem(product, request.quantity());
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Override
    public CartResponseDTO updateItemQuantity(UUID productId, UpdateCartItemRequestDTO request) {
        User user = userService.getCurrentUserEntity();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
        
        if (product.getStock() < request.quantity()) {
            throw new InsufficientStockException("Insufficient stock available");
        }

        cart.updateItemQuantity(productId, request.quantity());
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Override
    public CartResponseDTO removeItem(UUID productId) {
        User user = userService.getCurrentUserEntity();
        Cart cart = getOrCreateCart(user);
        cart.removeItem(productId);
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Override
    public void clearCart() {
        User user = userService.getCurrentUserEntity();
        cartRepository.findByUserId(user.getId()).ifPresent(cart -> {
            cart.clear();
            cartRepository.save(cart);
        });
    }

    /**
     * Retrieves the raw {@link Cart} entity for internal module processing.
     * <p>
     * Utilizes optimized fetch join to retrieve all items and product data in a single roundtrip.
     * </p>
     *
     * @param userId unique identifier of the user.
     * @return the {@link Cart} entity.
     * @throws CartNotFoundException if no cart is associated with the user.
     */
    @Override
    @Transactional(readOnly = true)
    public Cart getCartEntityForUser(UUID userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
    }

    /**
     * Ensures a cart exists for the given user, fetching it with items for performance.
     * * @param user the cart owner.
     * @return an existing or newly created {@link Cart}.
     */
    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserIdWithItems(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }
}