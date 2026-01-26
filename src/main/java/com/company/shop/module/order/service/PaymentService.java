package com.company.shop.module.order.service;

import com.company.shop.module.order.entity.Order;
import com.company.shop.module.order.dto.PaymentIntentResponseDTO;

public interface PaymentService {
    PaymentIntentResponseDTO createPaymentIntent(Order order);
    void handleWebhook(String payload, String sigHeader);
}