package com.ventrax.smartgn.support.interfaces.rest.controllers;

import com.ventrax.smartgn.support.application.internal.commandservices.SupportCommandService;
import com.ventrax.smartgn.support.application.internal.queryservices.SupportQueryService;
import com.ventrax.smartgn.support.domain.model.entities.Technician;
import com.ventrax.smartgn.support.interfaces.rest.resources.SupportResources.CreateTechnicianRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support/technicians")
public class TechnicianController {

    private final SupportCommandService commandService;
    private final SupportQueryService queryService;

    public TechnicianController(SupportCommandService commandService, SupportQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<Technician> createTechnician(@RequestBody CreateTechnicianRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.createTechnician(request.fullName(), request.accreditationCode(), request.email(), request.phone()));
    }

    @GetMapping
    public ResponseEntity<List<Technician>> getAvailableTechnicians() {
        return ResponseEntity.ok(queryService.getAvailableTechnicians());
    }
}