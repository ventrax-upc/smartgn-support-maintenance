package com.ventrax.smartgn.support.interfaces.rest.controllers;

import com.ventrax.smartgn.support.application.internal.commandservices.SupportCommandService;
import com.ventrax.smartgn.support.application.internal.queryservices.SupportQueryService;
import com.ventrax.smartgn.support.domain.model.aggregates.TechnicalVisit;
import com.ventrax.smartgn.support.domain.model.entities.TechnicianAssignment;
import com.ventrax.smartgn.support.interfaces.rest.resources.SupportResources.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/support")
public class VisitController {

    private final SupportCommandService commandService;
    private final SupportQueryService queryService;

    public VisitController(SupportCommandService commandService, SupportQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping("/tickets/{ticketId}/visits")
    public ResponseEntity<TechnicalVisit> requestVisit(@PathVariable UUID ticketId, @RequestBody RequestVisitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.requestVisit(ticketId, request.requestedWindowStart(), request.requestedWindowEnd()));
    }

    @GetMapping("/tickets/{ticketId}/visits")
    public ResponseEntity<List<TechnicalVisit>> getVisitsByTicket(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(queryService.getVisitsByTicket(ticketId));
    }

    @PostMapping("/visits/{visitId}/assignments")
    public ResponseEntity<TechnicianAssignment> assignTechnician(@PathVariable UUID visitId, @RequestBody AssignTechnicianRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.assignTechnician(visitId, request.technicianId()));
    }
}