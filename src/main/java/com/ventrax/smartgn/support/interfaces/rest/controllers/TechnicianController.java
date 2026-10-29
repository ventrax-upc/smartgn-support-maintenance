package com.ventrax.smartgn.support.interfaces.rest.controllers;

import com.ventrax.smartgn.support.application.internal.commandservices.SupportCommandService;
import com.ventrax.smartgn.support.application.internal.queryservices.SupportQueryService;
import com.ventrax.smartgn.support.domain.model.entities.Technician;
import com.ventrax.smartgn.support.interfaces.rest.resources.SupportResources.CreateTechnicianRequest;
import com.ventrax.smartgn.support.interfaces.rest.resources.SupportResources.UpdateTechnicianRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/support/technicians")
public class TechnicianController {

    private final SupportCommandService commandService;
    private final SupportQueryService queryService;

    public TechnicianController(SupportCommandService commandService, SupportQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping
    public ResponseEntity<List<Technician>> getTechnicians(
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String specialty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(queryService.getAvailableTechnicians());
    }

    @PostMapping
    public ResponseEntity<Technician> createTechnician(@RequestBody CreateTechnicianRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.createTechnician(
                        request.fullName(), request.accreditationCode(), request.specialty(), request.email(), request.phone()));
    }

    @PutMapping("/{technicianId}")
    public ResponseEntity<Technician> updateTechnician(
            @PathVariable UUID technicianId,
            @RequestBody UpdateTechnicianRequest request) {
        return ResponseEntity.ok(commandService.updateTechnician(
                technicianId, request.accreditationCode(), request.specialty(),
                request.email(), request.phone(), request.available(), request.active()));
    }
}