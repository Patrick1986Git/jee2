/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.system.dto.ApplicationStatusDTO;
import com.company.shop.module.system.service.ApplicationStatusService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * System Infrastructure Controller providing administrative and diagnostic endpoints.
 * <p>
 * This controller exposes metadata regarding the application's runtime state, 
 * versioning, and environment configuration. Access to these endpoints is typically 
 * monitored and restricted in production environments.
 * </p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/system")
@Tag(name = "System", description = "Endpoints for infrastructure and application health diagnostics")
public class SystemController {

    private final ApplicationStatusService statusService;

    /**
     * Constructs the controller with the necessary diagnostic service.
     *
     * @param statusService the service responsible for aggregating system status.
     */
    public SystemController(ApplicationStatusService statusService) {
        this.statusService = statusService;
    }

    /**
     * Retrieves the current application status and runtime metadata.
     * <p>
     * Provides a high-level overview of the service identity, active profiles, 
     * and underlying host information. Useful for health checks and CI/CD validation.
     * </p>
     *
     * @return a {@link ResponseEntity} containing the {@link ApplicationStatusDTO}.
     */
    @GetMapping("/status")
    @Operation(
        summary = "Get application status", 
        description = "Returns detailed technical metadata about the backend instance, including version and environment details."
    )
    public ResponseEntity<ApplicationStatusDTO> getStatus() {
        return ResponseEntity.ok(statusService.getApplicationStatus());
    }
}