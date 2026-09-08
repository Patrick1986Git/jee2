package com.company.shop.module.order.expiration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.company.shop.module.order.exception.StripeConfigurationException;
import com.stripe.StripeClient;
import com.stripe.service.PaymentIntentService;

class StripeClientConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(StripeClientConfiguration.class);

    @Test
    void configuration_shouldBindExplicitPolicyAndConstructClient() {
        contextRunner.withPropertyValues(
                "stripe.api-key=sk_test_value",
                "stripe.network.connect-timeout=PT3S",
                "stripe.network.read-timeout=PT7S",
                "stripe.network.max-network-retries=2")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(StripeClient.class);
                    assertThat(context).hasSingleBean(PaymentIntentService.class);
                    var properties = context.getBean(StripeNetworkProperties.class);
                    assertThat(properties.connectTimeoutMillis()).isEqualTo(3_000);
                    assertThat(properties.readTimeoutMillis()).isEqualTo(7_000);
                    assertThat(properties.maxNetworkRetries()).isEqualTo(2);
                });
    }

    @Test
    void configuration_shouldFailWhenRequiredNetworkPolicyIsMissing() {
        contextRunner.withPropertyValues("stripe.api-key=sk_test_value")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasMessageContaining("stripe.network");
                });
    }

    @Test
    void configuration_shouldFailWhenApiKeyIsBlank() {
        contextRunner.withPropertyValues(
                "stripe.api-key= ",
                "stripe.network.connect-timeout=PT3S",
                "stripe.network.read-timeout=PT7S",
                "stripe.network.max-network-retries=0")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure()
                        .rootCause()
                        .isInstanceOf(StripeConfigurationException.class));
    }

    @Test
    void stripeClientBuilder_shouldApplyTheCompleteNetworkPolicy() {
        var properties = properties(Duration.ofSeconds(3), Duration.ofSeconds(7), 2);

        var builder = StripeClientConfiguration.stripeClientBuilder("sk_test_value", properties);

        assertThat(builder.getConnectTimeout()).isEqualTo(3_000);
        assertThat(builder.getReadTimeout()).isEqualTo(7_000);
        assertThat(builder.getMaxNetworkRetries()).isEqualTo(2);
        assertThat(builder.getAuthenticator()).isNotNull();
    }

    @Test
    void connectTimeoutMillis_shouldRejectNonPositiveValue() {
        var properties = properties(Duration.ZERO, Duration.ofSeconds(1), 0);

        assertThatThrownBy(properties::connectTimeoutMillis)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("connect-timeout must be positive");
    }

    @Test
    void readTimeoutMillis_shouldRejectValuesOutsideSdkIntegerRange() {
        var properties = properties(Duration.ofSeconds(1), Duration.ofDays(30), 0);

        assertThatThrownBy(properties::readTimeoutMillis)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("read-timeout must fit in milliseconds");
    }

    @Test
    void stripeClientBuilder_shouldRejectNegativeRetryCount() {
        var properties = properties(Duration.ofSeconds(1), Duration.ofSeconds(1), -1);

        assertThatThrownBy(() -> StripeClientConfiguration.stripeClientBuilder("sk_test_value", properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max-network-retries must not be negative");
    }

    private StripeNetworkProperties properties(Duration connect, Duration read, int retries) {
        var properties = new StripeNetworkProperties();
        properties.setConnectTimeout(connect);
        properties.setReadTimeout(read);
        properties.setMaxNetworkRetries(retries);
        return properties;
    }
}
