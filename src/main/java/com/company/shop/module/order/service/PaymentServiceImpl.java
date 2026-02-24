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
import com.company.shop.module.order.exception.OrderNotFoundException;
import com.company.shop.module.order.exception.PaymentProcessingException;
import com.company.shop.module.order.exception.StripeConfigurationException;
import com.company.shop.module.order.exception.WebhookProcessingException;
import com.company.shop.module.order.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.annotation.PostConstruct;

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

    public PaymentServiceImpl(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new StripeConfigurationException("Stripe API key is missing in configuration.");
        }
        Stripe.apiKey = secretKey;
    }

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
            log.error("Stripe PaymentIntent creation failed for orderId={}", order.getId(), e);
            throw new PaymentProcessingException("Failed to initialize payment for order: " + order.getId());
        }
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        try {
            var event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if (!"payment_intent.succeeded".equals(event.getType())) {
                return;
            }

            var deserializer = event.getDataObjectDeserializer();
            PaymentIntent intent = (PaymentIntent) deserializer.getObject().orElse(null);
            if (intent == null) {
                return;
            }

            String orderId = intent.getMetadata().get("orderId");
            if (orderId == null || orderId.isBlank()) {
                return;
            }

            UUID parsedOrderId = UUID.fromString(orderId);
            Order order = orderRepo.findById(parsedOrderId).orElseThrow(() -> new OrderNotFoundException(parsedOrderId));
            order.markAsPaid();
            orderRepo.save(order);
        } catch (OrderNotFoundException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Stripe webhook processing failed", e);
            throw new WebhookProcessingException("Unable to process Stripe webhook event.");
        }
    }
}
