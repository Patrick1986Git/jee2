/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.order.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.order.dto.OrderCheckoutRequestDTO;
import com.company.shop.module.order.dto.OrderDetailedResponseDTO;
import com.company.shop.module.order.dto.OrderResponseDTO;
import com.company.shop.module.order.service.OrderService;

import jakarta.validation.Valid;

/**
 * REST controller for managing order lifecycle and fulfillment.
 * <p>
 * This controller provides endpoints for order placement (checkout), retrieval 
 * of personal order history, and administrative overview of all transactions.
 * All endpoints are secured and require appropriate authorization levels.
 * </p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Constructs the controller with the required {@link OrderService}.
     *
     * @param orderService service handling business logic for orders.
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Finalizes the shopping process by creating an order from the current user's cart.
     * <p>
     * This operation triggers stock validation, price calculation, and cart clearance 
     * upon successful placement.
     * </p>
     *
     * @param request DTO containing checkout details like discount codes and notes.
     * @return {@link OrderResponseDTO} containing basic order information.
     */
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public OrderResponseDTO placeOrder(@Valid @RequestBody OrderCheckoutRequestDTO request) {
        return orderService.placeOrderFromCart(request);
    }

    /**
     * Retrieves detailed information about a specific order.
     *
     * @param id unique identifier of the order.
     * @return {@link OrderDetailedResponseDTO} with line items and fulfillment status.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public OrderDetailedResponseDTO getOrderById(@PathVariable UUID id) {
        return orderService.findById(id);
    }

    /**
     * Fetches a paginated list of orders placed by the currently authenticated user.
     *
     * @param pageable pagination and sorting parameters.
     * @return a page of {@link OrderResponseDTO}s.
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Page<OrderResponseDTO> getMyOrders(@PageableDefault(size = 10) Pageable pageable) {
        return orderService.findMyOrders(pageable);
    }

    /**
     * Administrative endpoint to retrieve all orders across the system.
     * <p>
     * Access is restricted to users with {@code ROLE_ADMIN} authority.
     * </p>
     *
     * @param pageable pagination and sorting parameters.
     * @return a page of all orders for management purposes.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<OrderResponseDTO> getAllOrders(@PageableDefault(size = 20) Pageable pageable) {
        return orderService.findAll(pageable);
    }
}