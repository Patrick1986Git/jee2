package com.company.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Profile("prod")
public class ProductionTomcatCapacityValidator {

    private final int maximumThreads;
    private final int maximumConnections;
    private final int acceptCount;
    private final String connectionTimeout;

    public ProductionTomcatCapacityValidator(
            @Value("${server.tomcat.threads.max}") int maximumThreads,
            @Value("${server.tomcat.max-connections}") int maximumConnections,
            @Value("${server.tomcat.accept-count}") int acceptCount,
            @Value("${server.tomcat.connection-timeout}") String connectionTimeout) {
        this.maximumThreads = maximumThreads;
        this.maximumConnections = maximumConnections;
        this.acceptCount = acceptCount;
        this.connectionTimeout = connectionTimeout;
    }

    @PostConstruct
    void validate() {
        requirePositive(maximumThreads, "server.tomcat.threads.max");
        requirePositive(maximumConnections, "server.tomcat.max-connections");
        requirePositive(acceptCount, "server.tomcat.accept-count");
        var parsedConnectionTimeout = DurationStyle.detectAndParse(connectionTimeout);
        if (parsedConnectionTimeout.isZero() || parsedConnectionTimeout.isNegative()) {
            throw new IllegalStateException("server.tomcat.connection-timeout must be positive");
        }
    }

    private static void requirePositive(int value, String property) {
        if (value < 1) {
            throw new IllegalStateException(property + " must be positive");
        }
    }
}
