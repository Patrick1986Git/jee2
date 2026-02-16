/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.order.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.module.cart.entity.Cart;
import com.company.shop.module.cart.entity.CartItem;
import com.company.shop.module.cart.service.CartService;
import com.company.shop.module.order.dto.OrderCheckoutRequestDTO;
import com.company.shop.module.order.dto.OrderDetailedResponseDTO;
import com.company.shop.module.order.dto.OrderResponseDTO;
import com.company.shop.module.order.dto.PaymentIntentResponseDTO;
import com.company.shop.module.order.entity.DiscountCode;
import com.company.shop.module.order.entity.Order;
import com.company.shop.module.order.entity.OrderItem;
import com.company.shop.module.order.entity.Payment;
import com.company.shop.module.order.mapper.OrderMapper;
import com.company.shop.module.order.repository.DiscountCodeRepository;
import com.company.shop.module.order.repository.OrderRepository;
import com.company.shop.module.order.repository.PaymentRepository;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.product.repository.ProductRepository;
import com.company.shop.module.user.entity.User;
import com.company.shop.module.user.service.UserService;

import jakarta.persistence.EntityNotFoundException;

/**
 * Production implementation of {@link OrderService} managing high-integrity transactional order placement.
 * <p>
 * This implementation orchestrates complex business processes including:
 * <ul>
 * <li>Pessimistic locking of products to prevent overselling.</li>
 * <li>Atomic conversion of shopping carts to formalized orders.</li>
 * <li>Integration with external payment providers (Stripe).</li>
 * <li>Validation of domain invariants and security-scoped order retrieval.</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final PaymentRepository paymentRepo;
    private final DiscountCodeRepository discountCodeRepo;
    private final UserService userService;
    private final CartService cartService;
    private final OrderMapper mapper;
    private final PaymentService paymentService;

    /**
     * Constructs the service with comprehensive dependency injection.
     */
    public OrderServiceImpl(OrderRepository orderRepo, 
                            ProductRepository productRepo, 
                            PaymentRepository paymentRepo,
                            DiscountCodeRepository discountCodeRepo,
                            UserService userService, 
                            CartService cartService,
                            OrderMapper mapper, 
                            PaymentService paymentService) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.paymentRepo = paymentRepo;
        this.discountCodeRepo = discountCodeRepo;
        this.userService = userService;
        this.cartService = cartService;
        this.mapper = mapper;
        this.paymentService = paymentService;
    }

    /**
     * Places an order from the user's current cart.
     * <p>
     * Operation is fully transactional. If payment intent creation fails or stock is 
     * insufficient, the entire operation (including stock deduction) is rolled back.
     * </p>
     *
     * @param request checkout details including optional discount codes.
     * @return {@link OrderResponseDTO} enriched with Stripe payment intent data.
     * @throws IllegalStateException if cart is empty.
     * @throws EntityNotFoundException if products in cart are no longer available.
     */
    @Override
    @Transactional
    public OrderResponseDTO placeOrderFromCart(OrderCheckoutRequestDTO request) {
        User user = userService.getCurrentUserEntity();
        Cart cart = cartService.getCartEntityForUser(user.getId());

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place order: Cart is empty.");
        }

        Order order = new Order(user);

        // Process items with pessimistic locking to ensure stock integrity
        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepo.findByIdWithLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found or unavailable: " + cartItem.getProduct().getId()));

            product.decreaseStock(cartItem.getQuantity());

            OrderItem orderItem = new OrderItem(product, cartItem.getQuantity(), product.getPrice());
            order.addItem(orderItem);
        }

        // Apply business logic for discount codes
        if (request.discountCode() != null && !request.discountCode().isBlank()) {
            DiscountCode dc = discountCodeRepo.findByCodeIgnoreCaseAndDeletedFalse(request.discountCode().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or expired discount code"));
            
            order.applyDiscount(dc);
        }

        Order savedOrder = orderRepo.save(order);

        // Initialize payment record
        Payment payment = new Payment(savedOrder, "STRIPE", savedOrder.getTotalAmount());
        paymentRepo.save(payment);

        // Cleanup resources
        cartService.clearCart();
        
        // External integration
        PaymentIntentResponseDTO stripeInfo = paymentService.createPaymentIntent(savedOrder);

        OrderResponseDTO baseDto = mapper.toDto(savedOrder);
        return new OrderResponseDTO(
                baseDto.id(),
                baseDto.status(),
                baseDto.totalAmount(),
                baseDto.createdAt(),
                stripeInfo
        );
    }

    /**
     * Retrieves an order by its ID with strict ownership validation.
     *
     * @param id order identifier.
     * @return detailed order information.
     * @throws AccessDeniedException if a non-admin user attempts to view another user's order.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderDetailedResponseDTO findById(UUID id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));

        User currentUser = userService.getCurrentUserEntity();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !order.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to view this order.");
        }
        return mapper.toDetailedDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findAll(Pageable pageable) {
        return orderRepo.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findMyOrders(Pageable pageable) {
        User currentUser = userService.getCurrentUserEntity();
        return orderRepo.findByUser(currentUser, pageable).map(mapper::toDto);
    }
}