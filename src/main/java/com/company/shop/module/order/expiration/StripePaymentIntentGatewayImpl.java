package com.company.shop.module.order.expiration;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.service.PaymentIntentService;
import com.company.shop.module.order.service.StripeMinorUnitConverter;

@Component
public class StripePaymentIntentGatewayImpl implements StripePaymentIntentGateway {
    private final PaymentIntentService paymentIntents;

    public StripePaymentIntentGatewayImpl(PaymentIntentService paymentIntents) {
        this.paymentIntents = paymentIntents;
    }

    @Override
    public PaymentIntent create(UUID orderId, BigDecimal amount, String idempotencyKey) throws Exception {
        var params = PaymentIntentCreateParams.builder().setAmount(StripeMinorUnitConverter.fromPln(amount))
                .setCurrency("pln").putMetadata("orderId", orderId.toString()).build();
        var options = RequestOptions.builder().setIdempotencyKey(idempotencyKey).build();
        return paymentIntents.create(params, options);
    }
    @Override
    public PaymentIntent retrieve(String paymentIntentId) throws Exception {
        return paymentIntents.retrieve(paymentIntentId);
    }
    @Override
    public PaymentIntent cancelAsAbandoned(PaymentIntent intent, String idempotencyKey) throws Exception {
        var params = PaymentIntentCancelParams.builder()
                .setCancellationReason(PaymentIntentCancelParams.CancellationReason.ABANDONED).build();
        var options = RequestOptions.builder().setIdempotencyKey(idempotencyKey).build();
        return paymentIntents.cancel(intent.getId(), params, options);
    }
}
