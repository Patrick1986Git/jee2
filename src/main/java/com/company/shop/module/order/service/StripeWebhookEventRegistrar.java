package com.company.shop.module.order.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import com.company.shop.module.order.repository.StripeWebhookEventRepository;

@Service
public class StripeWebhookEventRegistrar {

    private final StripeWebhookEventRepository stripeWebhookEventRepository;

    public StripeWebhookEventRegistrar(StripeWebhookEventRepository stripeWebhookEventRepository) {
        this.stripeWebhookEventRepository = stripeWebhookEventRepository;
    }

    public boolean register(String eventId, String eventType) {
        int insertedRows = stripeWebhookEventRepository.insertIgnoreDuplicate(
                UUID.randomUUID(),
                eventId,
                eventType,
                LocalDateTime.now());
        return insertedRows == 1;
    }
}
