package com.company.shop.module.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.system.dto.ApplicationStatusDTO;
import com.company.shop.module.system.service.ApplicationStatusService;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private final ApplicationStatusService statusService;

    public SystemController(ApplicationStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    public ResponseEntity<ApplicationStatusDTO> getSystemStatus() {
        return ResponseEntity.ok(statusService.getApplicationStatus());
    }
}
