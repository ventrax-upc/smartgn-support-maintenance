package com.ventrax.support.interfaces.rest;

import com.ventrax.support.application.internal.commandservices.CoverageService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "serviceAuth")
@RestController
public class IntegrationController {
    private final CoverageService service;

    public IntegrationController(CoverageService service) {
        this.service = service;
    }

    @PostMapping("/internal/v1/events")
    public void receive(@RequestBody Map<String, Object> event) {
        service.receive(event);
    }
}
