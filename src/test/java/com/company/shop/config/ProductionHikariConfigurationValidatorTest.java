package com.company.shop.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.Test;

class ProductionHikariConfigurationValidatorTest {

    @Test
    void validate_shouldAcceptValidDeploymentOwnedSettings() {
        var validator = new ProductionHikariConfigurationValidator(12, 3, 1_000);

        assertThatCode(validator::validate).doesNotThrowAnyException();
    }

    @Test
    void validate_shouldRejectNonPositiveMaximumPoolSize() {
        var validator = new ProductionHikariConfigurationValidator(0, 0, 1_000);

        assertThatIllegalStateException()
                .isThrownBy(validator::validate)
                .withMessage("spring.datasource.hikari.maximum-pool-size must be positive");
    }

    @Test
    void validate_shouldRejectNegativeMinimumIdle() {
        var validator = new ProductionHikariConfigurationValidator(4, -1, 1_000);

        assertThatIllegalStateException()
                .isThrownBy(validator::validate)
                .withMessage("spring.datasource.hikari.minimum-idle must not be negative");
    }

    @Test
    void validate_shouldRejectMinimumIdleAboveMaximumPoolSize() {
        var validator = new ProductionHikariConfigurationValidator(4, 5, 1_000);

        assertThatIllegalStateException()
                .isThrownBy(validator::validate)
                .withMessage("spring.datasource.hikari.minimum-idle must not exceed "
                        + "spring.datasource.hikari.maximum-pool-size");
    }

    @Test
    void validate_shouldRejectConnectionTimeoutBelowHikariMinimum() {
        var validator = new ProductionHikariConfigurationValidator(4, 0, 249);

        assertThatIllegalStateException()
                .isThrownBy(validator::validate)
                .withMessage("spring.datasource.hikari.connection-timeout must be at least 250 milliseconds");
    }
}
