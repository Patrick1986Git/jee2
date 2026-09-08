package com.company.shop.module.order.expiration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.stripe.StripeClient;
import com.stripe.service.PaymentIntentService;

@Configuration
public class StripeClientConfiguration {
    @Bean
    StripeClient stripeClient(@Value("${stripe.api-key}") String apiKey, StripeNetworkProperties network) {
        return stripeClientBuilder(apiKey, network).build();
    }

    static StripeClient.StripeClientBuilder stripeClientBuilder(String apiKey, StripeNetworkProperties network) {
        return StripeClient.builder()
                .setApiKey(apiKey)
                .setConnectTimeout(network.connectTimeoutMillis())
                .setReadTimeout(network.readTimeoutMillis())
                .setMaxNetworkRetries(network.maxNetworkRetries());
    }

    @Bean
    PaymentIntentService stripePaymentIntents(StripeClient stripeClient) {
        return stripeClient.v1().paymentIntents();
    }
}
