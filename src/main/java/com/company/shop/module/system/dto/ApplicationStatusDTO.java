/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.system.dto;

import java.time.Instant;
import java.util.List;

/**
 * A comprehensive Data Transfer Object representing the operational status 
 * and diagnostic metadata of the application instance.
 * <p>
 * This record is used by monitoring tools and administrative dashboards to verify 
 * the current state of the system, including environmental variables, 
 * active profiles, and host identification.
 * </p>
 *
 * @param applicationName the official name of the service as defined in configuration.
 * @param version         the current build version (e.g., from Maven/Gradle).
 * @param environment     the logical environment name (e.g., dev, staging, prod).
 * @param serverTime      the current UTC timestamp from the server's clock.
 * @param activeProfiles  the list of Spring profiles active during the current runtime.
 * @param javaVersion     the version of the Java Runtime Environment (JRE).
 * @param osName          the name of the operating system hosting the application.
 * @param osVersion       the version/kernel of the operating system.
 * @param hostName        the network identifier of the server or container instance.
 * * @since 1.0.0
 */
public record ApplicationStatusDTO(
        String applicationName,
        String version,
        String environment,
        Instant serverTime,
        List<String> activeProfiles,
        String javaVersion,
        String osName,
        String osVersion,
        String hostName
) {
}