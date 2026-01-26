package com.company.shop.module.order.service;

import com.company.shop.module.order.dto.PaymentIntentResponseDTO;
import com.company.shop.module.order.entity.Order;
import com.company.shop.module.order.entity.OrderStatus;
import com.company.shop.module.order.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.api-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final OrderRepository orderRepo;

    public PaymentServiceImpl(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
        Stripe.apiKey = secretKey;
    }

    @Override
    public PaymentIntentResponseDTO createPaymentIntent(Order order) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(order.getTotalAmount().movePointRight(2).longValue()) // Stripe liczy w groszach/centach
                .setCurrency("pln")
                .putMetadata("orderId", order.getId().toString())
                .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return new PaymentIntentResponseDTO(intent.getClientSecret(), "twój_klucz_publiczny");
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas tworzenia płatności Stripe", e);
        }
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        try {
            var event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
                String orderId = intent.getMetadata().get("orderId");
                
                orderRepo.findById(java.util.UUID.fromString(orderId)).ifPresent(order -> {
                    order.setStatus(OrderStatus.PAID);
                    orderRepo.save(order);
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Stripe webhook error", e);
        }
    }
}