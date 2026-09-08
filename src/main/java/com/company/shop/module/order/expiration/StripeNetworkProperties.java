package com.company.shop.module.order.expiration;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Validated
@ConfigurationProperties(prefix = "stripe.network")
public class StripeNetworkProperties {
    @NotNull
    private Duration connectTimeout;
    @NotNull
    private Duration readTimeout;
    @PositiveOrZero
    private int maxNetworkRetries;

    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
    public int getMaxNetworkRetries() { return maxNetworkRetries; }
    public void setMaxNetworkRetries(int maxNetworkRetries) { this.maxNetworkRetries = maxNetworkRetries; }

    public int connectTimeoutMillis() { return positiveMillis(connectTimeout, "connect-timeout"); }
    public int readTimeoutMillis() { return positiveMillis(readTimeout, "read-timeout"); }
    public int maxNetworkRetries() {
        if (maxNetworkRetries < 0) throw new IllegalStateException("stripe.network.max-network-retries must not be negative");
        return maxNetworkRetries;
    }

    private int positiveMillis(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalStateException("stripe.network." + name + " must be positive");
        }
        try {
            return Math.toIntExact(value.toMillis());
        } catch (ArithmeticException ex) {
            throw new IllegalStateException("stripe.network." + name + " must fit in milliseconds", ex);
        }
    }
}
