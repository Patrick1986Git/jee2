/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.env.Environment;

/**
 * Main entry point for the <strong>Shop Service</strong> application.
 * <p>
 * This class bootstraps the Spring Boot application context, enabling auto-configuration,
 * component scanning, and configuration property binding. It also provides 
 * diagnostic logging upon successful startup.
 * </p>
 *
 * @since 1.0.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ShopApplication {

    private static final Logger log = LoggerFactory.getLogger(ShopApplication.class);

    /**
     * Bootstraps the application and initializes the Spring context.
     *
     * @param args command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ShopApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    /**
     * Logs technical details about the running application instance.
     * <p>
     * Displays local and external URLs, the context path, active Spring profiles, 
     * and the Swagger UI documentation endpoint for improved developer experience.
     * </p>
     *
     * @param env the application {@link Environment} containing configuration properties.
     */
    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store"))
                .map(key -> "https")
                .orElse("http");
        
        String serverPort = Optional.ofNullable(env.getProperty("server.port")).orElse("8080");
        String contextPath = Optional.ofNullable(env.getProperty("server.servlet.context-path")).orElse("");
        String hostAddress = "localhost";
        
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        
        log.info("""
                
                ----------------------------------------------------------
                \tApplication '{}' is running! Access URLs:
                \tLocal: \t\t{}://localhost:{}{}
                \tExternal: \t{}://{}:{}{}
                \tSwagger UI: \t{}://localhost:{}{}/swagger-ui/index.html
                \tProfile(s): \t{}
                ----------------------------------------------------------""",
                env.getProperty("spring.application.name"),
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                protocol, serverPort, contextPath,
                env.getActiveProfiles().length == 0 ? "default" : env.getActiveProfiles());
    }
}