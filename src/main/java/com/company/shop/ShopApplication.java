/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main entry point for the <strong>Shop Service</strong> application.
 * <p>
 * This class bootstraps the Spring Boot application context, enabling auto-configuration
 * and scanning for configuration properties classes.
 * </p>
 *
 * @see ConfigurationPropertiesScan
 * @since 1.0.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ShopApplication {

    /**
     * Launches the Shop Service application.
     *
     * @param args command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}