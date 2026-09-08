package com.company.shop.module.order.expiration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.service.PaymentIntentService;

class StripePaymentIntentGatewayImplTest {
    private final PaymentIntentService paymentIntents = mock(PaymentIntentService.class);
    private final StripePaymentIntentGatewayImpl gateway = new StripePaymentIntentGatewayImpl(paymentIntents);

    @Test
    void create_shouldPreserveIdempotencyKeyOnConfiguredClient() throws Exception {
        var options = ArgumentCaptor.forClass(RequestOptions.class);
        var result = new PaymentIntent();
        when(paymentIntents.create(any(PaymentIntentCreateParams.class), any(RequestOptions.class))).thenReturn(result);

        assertThat(gateway.create(UUID.randomUUID(), new BigDecimal("10.00"), "order-payment-intent-1"))
                .isSameAs(result);

        verify(paymentIntents).create(any(PaymentIntentCreateParams.class), options.capture());
        assertThat(options.getValue().getIdempotencyKey()).isEqualTo("order-payment-intent-1");
    }

    @Test
    void retrieve_shouldUseConfiguredClientService() throws Exception {
        var result = new PaymentIntent();
        when(paymentIntents.retrieve("pi_1")).thenReturn(result);

        assertThat(gateway.retrieve("pi_1")).isSameAs(result);
        verify(paymentIntents).retrieve("pi_1");
    }

    @Test
    void cancel_shouldPreserveIdempotencyKeyOnConfiguredClient() throws Exception {
        var intent = new PaymentIntent();
        intent.setId("pi_1");
        var result = new PaymentIntent();
        var options = ArgumentCaptor.forClass(RequestOptions.class);
        when(paymentIntents.cancel(eq("pi_1"), any(PaymentIntentCancelParams.class), any(RequestOptions.class)))
                .thenReturn(result);

        assertThat(gateway.cancelAsAbandoned(intent, "order-reservation-expiration-1")).isSameAs(result);

        verify(paymentIntents).cancel(eq("pi_1"), any(PaymentIntentCancelParams.class), options.capture());
        assertThat(options.getValue().getIdempotencyKey()).isEqualTo("order-reservation-expiration-1");
    }
}
