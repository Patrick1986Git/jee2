/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.order.service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.module.cart.service.CartService;
import com.company.shop.module.order.dto.PaymentIntentResponseDTO;
import com.company.shop.module.order.entity.Order;
import com.company.shop.module.order.entity.OrderStatus;
import com.company.shop.module.order.exception.OrderNotFoundException;
import com.company.shop.module.order.exception.PaymentProcessingException;
import com.company.shop.module.order.exception.StripeConfigurationException;
import com.company.shop.module.order.exception.WebhookProcessingException;
import com.company.shop.module.order.exception.WebhookSignatureInvalidException;
import com.company.shop.module.order.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
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
    private final CartService cartService;

    public PaymentServiceImpl(OrderRepository orderRepo, CartService cartService) {
        this.orderRepo = orderRepo;
        this.cartService = cartService;
    }

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isBlank()) {
            log.error("Stripe API key is missing in configuration.");
            throw new StripeConfigurationException("Stripe API key is missing in configuration.");
        }
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.error("Stripe webhook secret is missing in configuration.");
            throw new StripeConfigurationException("Stripe webhook secret is missing in configuration.");
        }
        if (publicKey == null || publicKey.isBlank()) {
            log.error("Stripe public key is missing in configuration.");
            throw new StripeConfigurationException("Stripe public key is missing in configuration.");
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

            RequestOptions requestOptions = RequestOptions.builder()
                    .setIdempotencyKey("order-payment-intent-" + order.getId())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params, requestOptions);
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
                throw new WebhookSignatureInvalidException("Missing orderId metadata in Stripe webhook.");
            }

            UUID parsedOrderId = UUID.fromString(orderId);
            Order order = orderRepo.findByIdForUpdate(parsedOrderId)
                    .orElseThrow(() -> new OrderNotFoundException(parsedOrderId));

            if (order.getStatus() == OrderStatus.PAID) {
                log.info("Ignoring duplicate payment webhook for already paid orderId={}", order.getId());
                return;
            }

            validatePaymentIntentMatchesOrder(intent, order);

            order.markAsPaid();
            orderRepo.save(order);
            cartService.clearCartForUser(order.getUser().getId());
        } catch (com.stripe.exception.SignatureVerificationException | IllegalArgumentException ex) {
            log.warn("Invalid Stripe webhook payload/signature", ex);
            throw new WebhookSignatureInvalidException();
        } catch (OrderNotFoundException | WebhookSignatureInvalidException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Stripe webhook processing failed", e);
            throw new WebhookProcessingException("Unable to process Stripe webhook event.");
        }
    }

    private void validatePaymentIntentMatchesOrder(PaymentIntent intent, Order order) {
        Long amount = intent.getAmountReceived() != null && intent.getAmountReceived() > 0
                ? intent.getAmountReceived()
                : intent.getAmount();

        if (amount == null) {
            throw new WebhookSignatureInvalidException("Stripe webhook does not contain payment amount.");
        }

        long expectedAmount = order.getTotalAmount().movePointRight(2).longValue();
        if (amount.longValue() != expectedAmount) {
            throw new WebhookSignatureInvalidException(
                    "Stripe webhook payment amount does not match order total.");
        }

        String currency = intent.getCurrency();
        if (currency == null || !"pln".equals(currency.toLowerCase(Locale.ROOT))) {
            throw new WebhookSignatureInvalidException("Stripe webhook currency does not match expected currency.");
        }

        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new WebhookProcessingException("Order amount is invalid for payment reconciliation.");
        }
    }
}
