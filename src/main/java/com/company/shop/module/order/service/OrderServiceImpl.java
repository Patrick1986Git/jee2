/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.order.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.module.order.dto.OrderCreateRequestDTO;
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
 * Implementation of the {@link OrderService} providing business logic for order management.
 * <p>
 * This service handles the complete order lifecycle, including stock validation, 
 * discount application, and integration with payment gateways. 
 * All write operations are wrapped in a transaction to ensure data integrity.
 * </p>
 *
 * @since 1.0.0
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final PaymentRepository paymentRepo;
    private final DiscountCodeRepository discountCodeRepo;
    private final UserService userService;
    private final OrderMapper mapper;
    private final PaymentService paymentService;

    public OrderServiceImpl(OrderRepository orderRepo, 
                            ProductRepository productRepo, 
                            PaymentRepository paymentRepo,
                            DiscountCodeRepository discountCodeRepo,
                            UserService userService, 
                            OrderMapper mapper, 
                            PaymentService paymentService) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.paymentRepo = paymentRepo;
        this.discountCodeRepo = discountCodeRepo;
        this.userService = userService;
        this.mapper = mapper;
        this.paymentService = paymentService;
    }

    /**
     * Retrieves detailed information about a specific order.
     * * @param id the unique identifier of the order.
     * @return the detailed order data transfer object.
     * @throws EntityNotFoundException if the order does not exist.
     * @throws AccessDeniedException if the current user is not authorized to view the order.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderDetailedResponseDTO findById(UUID id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        User currentUser = userService.getCurrentUserEntity();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        boolean isOwner = order.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Unauthorized access to order data");
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

    /**
     * Processes and persists a new customer order.
     * <p>
     * The process includes:
     * <ul>
     * <li>Validating stock availability</li>
     * <li>Updating product inventory levels</li>
     * <li>Applying promotional discounts if a valid code is provided</li>
     * <li>Initiating a payment intent via {@link PaymentService}</li>
     * </ul>
     * </p>
     *
     * @param request the order creation details.
     * @return a summary of the created order including payment gateway information.
     * @throws IllegalStateException if stock is insufficient or discount code is invalid.
     */
    @Override
    @Transactional
    public OrderResponseDTO placeOrder(OrderCreateRequestDTO request) {
        User user = userService.getCurrentUserEntity();
        Order order = new Order(user, BigDecimal.ZERO);
        BigDecimal subtotal = BigDecimal.ZERO;

        for (var itemRequest : request.getItems()) {
            Product product = productRepo.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemRequest.getProductId()));

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            product.updateStock(product.getStock() - itemRequest.getQuantity());
            BigDecimal priceAtPurchase = product.getPrice();
            BigDecimal lineTotal = priceAtPurchase.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            order.addItem(new OrderItem(product, itemRequest.getQuantity(), priceAtPurchase));
        }

        BigDecimal finalAmount = applyDiscountIfApplicable(request.getDiscountCode(), subtotal, order);

        order.setTotalAmount(finalAmount);
        Order savedOrder = orderRepo.save(order);

        Payment payment = new Payment(savedOrder, "STRIPE", finalAmount);
        paymentRepo.save(payment);

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
     * Internal logic to validate and apply a discount code to the order subtotal.
     *
     * @param code the discount code string.
     * @param subtotal the initial amount before discount.
     * @param order the current order entity.
     * @return the final calculated amount after applying the discount.
     */
    private BigDecimal applyDiscountIfApplicable(String code, BigDecimal subtotal, Order order) {
        if (code == null || code.isBlank()) {
            return subtotal;
        }

        DiscountCode dc = discountCodeRepo.findByCodeIgnoreCaseAndDeletedFalse(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid discount code"));

        if (!dc.canBeUsed()) {
            throw new IllegalStateException("Discount code expired or limit reached");
        }

        BigDecimal discountMultiplier = BigDecimal.valueOf(100 - dc.getDiscountPercent())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        
        BigDecimal finalAmount = subtotal.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);

        dc.incrementUsage();

        return finalAmount;
    }
}