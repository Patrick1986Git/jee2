/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.order.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.module.order.dto.PaymentIntentResponseDTO;
import com.company.shop.module.order.entity.Order;
import com.company.shop.module.order.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.annotation.PostConstruct;

/**
 * Enterprise-grade implementation of {@link PaymentService} utilizing Stripe API.
 * <p>
 * This service handles the creation of payment intents and processes incoming 
 * webhooks from Stripe to maintain order payment status synchronization. 
 * It ensures that monetary values are correctly converted to Stripe's zero-decimal 
 * or smallest currency unit format (e.g., cents/groszy).
 * </p>
 *
 * @since 1.0.0
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Value("${stripe.api-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.public-key}")
    private String publicKey;

    private final OrderRepository orderRepo;

    /**
     * Constructs the service with required persistence dependencies.
     *
     * @param orderRepo repository for order status updates.
     */
    public PaymentServiceImpl(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    /**
     * Initializes the Stripe global configuration.
     * <p>
     * This method validates the presence of the secret key at startup to prevent 
     * runtime failures during transaction processing.
     * </p>
     *
     * @throws IllegalStateException if the API key is missing or blank.
     */
    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Stripe API Key is missing in configuration!");
        }
        Stripe.apiKey = secretKey;
    }

    /**
     * Creates a new PaymentIntent for a specific {@link Order}.
     * <p>
     * Converts the {@link Order#getTotalAmount()} to minor units (e.g., grosze) 
     * and attaches order identification via metadata for asynchronous reconciliation.
     * </p>
     *
     * @param order the order aggregate for which payment is being requested.
     * @return {@link PaymentIntentResponseDTO} containing the client secret for front-end SDK.
     * @throws IllegalStateException if the Stripe API request fails.
     */
    @Override
    public PaymentIntentResponseDTO createPaymentIntent(Order order) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(order.getTotalAmount().movePointRight(2).longValue())
                    .setCurrency("pln")
                    .putMetadata("orderId", order.getId().toString())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return new PaymentIntentResponseDTO(intent.getClientSecret(), publicKey);
        } catch (Exception e) {
            throw new IllegalStateException("Error creating Stripe PaymentIntent: " + e.getMessage(), e);
        }
    }

    /**
     * Processes verified webhook events received from Stripe.
     * <p>
     * Upon receiving a {@code payment_intent.succeeded} event, this method 
     * transitions the associated order to the PAID status within a transaction.
     * </p>
     *
     * @param payload   raw JSON payload from the request body.
     * @param sigHeader the {@code Stripe-Signature} header for event verification.
     * @throws IllegalStateException if the signature is invalid or processing fails.
     */
    @Override
    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        try {
            var event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if ("payment_intent.succeeded".equals(event.getType())) {
                var deserializer = event.getDataObjectDeserializer();
                PaymentIntent intent = (PaymentIntent) deserializer.getObject().orElse(null);

                if (intent != null) {
                    String orderId = intent.getMetadata().get("orderId");
                    if (orderId != null) {
                        orderRepo.findById(UUID.fromString(orderId)).ifPresent(order -> {
                            order.markAsPaid();
                            orderRepo.save(order);
                        });
                    }
                }
            }
        } catch (Exception e) {
            log.error("Stripe webhook processing error: {}", e.getMessage(), e);
            throw new IllegalStateException("Stripe webhook processing error: " + e.getMessage(), e);
        }
    }
}