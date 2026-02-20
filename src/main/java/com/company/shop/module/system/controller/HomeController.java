/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Root level controller for basic API heartbeat and landing information.
 * <p>
 * This controller provides a public entry point to verify the availability 
 * of the HTTP stack and serves as a minimal health check endpoint before 
 * more complex diagnostics are performed.
 * </p>
 *
 * @since 1.0.0
 */
@RestController
@Tag(name = "System", description = "Endpoints for infrastructure and application health diagnostics")
public class HomeController {

    /**
     * Provides a basic availability confirmation message.
     * <p>
     * Often used by health monitoring systems and developers to ensure the 
     * application context is refreshed and accepting requests.
     * </p>
     *
     * @return a plain text confirmation of the API status.
     */
    @GetMapping("/")
    @Operation(
        summary = "API Welcome Status", 
        description = "Returns a simple confirmation message indicating that the Enterprise Shop API is operational."
    )
    public String home() {
        return "Enterprise Shop API is running!";
    }
}