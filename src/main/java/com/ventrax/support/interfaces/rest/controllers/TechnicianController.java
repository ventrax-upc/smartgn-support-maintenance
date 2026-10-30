package com.ventrax.support.interfaces.rest.controllers;

import com.ventrax.support.application.internal.commandservices.SupportCommandService;
import com.ventrax.support.application.internal.queryservices.SupportQueryService;
import com.ventrax.support.domain.model.entities.Technician;
import com.ventrax.support.interfaces.rest.resources.SupportResources.CreateTechnicianRequest;
import com.ventrax.support.interfaces.rest.resources.SupportResources.UpdateTechnicianRequest;

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

    public TechnicianController(
            SupportCommandService commandService, SupportQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping
    public ResponseEntity<List<Technician>> getTechnicians(
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String specialty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0 || size < 1 || size > 100)
            throw new IllegalArgumentException("Invalid pagination");
        var result =
                queryService.technicians().stream()
                        .filter(t -> Boolean.TRUE.equals(t.getActive()))
                        .filter(t -> available == null || available.equals(t.getAvailable()))
                        .filter(
                                t ->
                                        specialty == null
                                                || specialty.equalsIgnoreCase(t.getSpecialty()))
                        .sorted(java.util.Comparator.comparing(Technician::getTechnicianId))
                        .toList();
        return ResponseEntity.ok()
                .header("X-Total-Count", Integer.toString(result.size()))
                .body(result.stream().skip((long) page * size).limit(size).toList());
    }

    @GetMapping("/{technicianId}")
    public ResponseEntity<Technician> get(@PathVariable UUID technicianId) {
        return queryService
                .technician(technicianId)
                .filter(t -> Boolean.TRUE.equals(t.getActive()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Technician> createTechnician(
            @jakarta.validation.Valid @RequestBody CreateTechnicianRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        commandService.createTechnician(
                                request.fullName(),
                                request.accreditationCode(),
                                request.specialty(),
                                request.email(),
                                request.phone()));
    }

    @PutMapping("/{technicianId}")
    public ResponseEntity<Technician> updateTechnician(
            @PathVariable UUID technicianId,
            @jakarta.validation.Valid @RequestBody UpdateTechnicianRequest request) {
        return ResponseEntity.ok(
                commandService.updateTechnician(
                        technicianId,
                        request.accreditationCode(),
                        request.specialty(),
                        request.email(),
                        request.phone(),
                        request.available(),
                        request.active()));
    }
}
