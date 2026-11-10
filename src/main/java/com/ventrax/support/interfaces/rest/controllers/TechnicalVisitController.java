package com.ventrax.support.interfaces.rest.controllers;

import com.ventrax.support.application.internal.commandservices.SupportCommandService;
import com.ventrax.support.application.internal.queryservices.SupportQueryService;
import com.ventrax.support.domain.model.aggregates.TechnicalVisit;
import com.ventrax.support.domain.model.entities.TechnicianAssignment;
import com.ventrax.support.domain.model.entities.VisitDiagnosis;
import com.ventrax.support.interfaces.rest.resources.SupportResources.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/support")
public class TechnicalVisitController {

    private final SupportCommandService commandService;
    private final SupportQueryService queryService;

    public TechnicalVisitController(
            SupportCommandService commandService, SupportQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping("/tickets/{ticketId}/visits")
    public ResponseEntity<TechnicalVisit> requestVisit(
            @PathVariable UUID ticketId,
            @jakarta.validation.Valid @RequestBody RequestVisitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        commandService.requestVisit(
                                ticketId,
                                request.requestedWindowStart(),
                                request.requestedWindowEnd()));
    }

    @GetMapping("/tickets/{ticketId}/visits")
    public ResponseEntity<List<TechnicalVisit>> getVisitsByTicket(
            @PathVariable UUID ticketId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return CollectionsPage.of(queryService.getVisitsByTicket(ticketId), page, size);
    }

    @PostMapping("/visits/{visitId}/schedule")
    public TechnicalVisit schedule(
            @PathVariable UUID visitId,
            @jakarta.validation.Valid @RequestBody RequestVisitRequest request) {
        return commandService.schedule(
                visitId, request.requestedWindowStart(), request.requestedWindowEnd());
    }

    @PostMapping("/visits/{visitId}/start")
    public TechnicalVisit start(@PathVariable UUID visitId) {
        return commandService.start(visitId);
    }

    @PostMapping("/visits/{visitId}/assign")
    public ResponseEntity<TechnicianAssignment> assignTechnician(
            @PathVariable UUID visitId,
            @jakarta.validation.Valid @RequestBody AssignTechnicianRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        commandService.assignTechnician(
                                visitId, request.technicianId(), request.reason()));
    }

    @PostMapping("/visits/{visitId}/complete")
    public ResponseEntity<VisitDiagnosis> completeVisit(
            @PathVariable UUID visitId,
            @jakarta.validation.Valid @RequestBody CompleteVisitDiagnosisRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        commandService.completeVisitWithDiagnosis(
                                visitId,
                                request.performedWork(),
                                request.diagnosis(),
                                request.finalCost()));
    }

    @GetMapping("/visits/{visitId}/diagnosis")
    public ResponseEntity<VisitDiagnosis> getDiagnosis(@PathVariable UUID visitId) {
        return queryService
                .getDiagnosisByVisit(visitId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
