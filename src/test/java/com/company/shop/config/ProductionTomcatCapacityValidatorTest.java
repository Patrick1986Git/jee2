package com.company.shop.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ProductionTomcatCapacityValidatorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ProductionTomcatCapacityValidator.class)
            .withPropertyValues(
                    "spring.profiles.active=prod",
                    "server.tomcat.threads.max=32",
                    "server.tomcat.max-connections=128",
                    "server.tomcat.accept-count=16",
                    "server.tomcat.connection-timeout=5s");

    @Test
    void prodContext_shouldRegisterAndRunValidatorForValidSettings() {
        contextRunner.run(context -> {
            assertThatCode(() -> context.getBean(ProductionTomcatCapacityValidator.class))
                    .doesNotThrowAnyException();
            assertThat(context).hasNotFailed();
        });
    }

    @Test
    void prodContext_shouldFailWhenMaximumThreadsIsNotPositive() {
        contextRunner.withPropertyValues("server.tomcat.threads.max=0")
                .run(context -> assertThat(context.getStartupFailure())
                        .hasRootCauseMessage("server.tomcat.threads.max must be positive"));
    }

    @Test
    void validate_shouldRejectUnboundedMaximumConnections() {
        var validator = new ProductionTomcatCapacityValidator(32, -1, 16, "5s");

        assertThatIllegalStateException()
                .isThrownBy(validator::validate)
                .withMessage("server.tomcat.max-connections must be positive");
    }

    @Test
    void validate_shouldRejectNonPositiveAcceptCount() {
        var validator = new ProductionTomcatCapacityValidator(32, 128, 0, "5s");

        assertThatIllegalStateException()
                .isThrownBy(validator::validate)
                .withMessage("server.tomcat.accept-count must be positive");
    }

    @Test
    void validate_shouldRejectNonPositiveConnectionTimeout() {
        var validator = new ProductionTomcatCapacityValidator(32, 128, 16, "0s");

        assertThatIllegalStateException()
                .isThrownBy(validator::validate)
                .withMessage("server.tomcat.connection-timeout must be positive");
    }
}
