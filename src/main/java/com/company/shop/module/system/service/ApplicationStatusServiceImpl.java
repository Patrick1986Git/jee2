/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.system.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.company.shop.module.system.dto.ApplicationStatusDTO;

/**
 * Production-ready implementation of {@link ApplicationStatusService}.
 * <p>
 * This service aggregates diagnostic data from the Spring Environment, 
 * Maven build properties, and Java system properties. It is designed to 
 * provide a comprehensive overview of the application's runtime context.
 * </p>
 *
 * @since 1.0.0
 */
@Service
public class ApplicationStatusServiceImpl implements ApplicationStatusService {

    private final Environment environment;
    private final BuildProperties buildProperties;

    /**
     * Constructs the service with required infrastructure dependencies.
     *
     * @param environment     the Spring {@link Environment} to access profiles and properties.
     * @param buildProperties the {@link BuildProperties} providing versioning metadata.
     */
    public ApplicationStatusServiceImpl(Environment environment, BuildProperties buildProperties) {
        this.environment = environment;
        this.buildProperties = buildProperties;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Collects real-time metrics including UTC server time, active profiles, 
     * and underlying OS/JRE specifications.
     * </p>
     */
    @Override
    public ApplicationStatusDTO getApplicationStatus() {
        List<String> profiles = Arrays.asList(environment.getActiveProfiles());
        
        // Hostname identification logic (Cloud/Container friendly)
        String hostName = System.getenv("HOSTNAME") != null 
                ? System.getenv("HOSTNAME") 
                : "localhost";

        return new ApplicationStatusDTO(
                environment.getProperty("spring.application.name", "enterprise-shop"),
                buildProperties.getVersion(),
                profiles.isEmpty() ? "default" : profiles.get(0),
                Instant.now(),
                profiles,
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                hostName
        );
    }
}