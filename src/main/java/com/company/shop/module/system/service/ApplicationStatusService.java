/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.system.service;

import com.company.shop.module.system.dto.ApplicationStatusDTO;

/**
 * Service interface for retrieving infrastructure and application-level 
 * diagnostic information.
 * <p>
 * This contract defines the operations necessary to audit the current 
 * state of the application instance, providing insights into the runtime 
 * environment and system metadata.
 * </p>
 *
 * @since 1.0.0
 */
public interface ApplicationStatusService {

    /**
     * Aggregates and retrieves the current operational status of the application.
     * <p>
     * This method collects data from various system sources, including 
     * the Spring {@link org.springframework.core.env.Environment} and 
     * Java {@link java.lang.System} properties.
     * </p>
     *
     * @return a {@link ApplicationStatusDTO} containing a point-in-time 
     * snapshot of the system diagnostics.
     */
    ApplicationStatusDTO getApplicationStatus();
}