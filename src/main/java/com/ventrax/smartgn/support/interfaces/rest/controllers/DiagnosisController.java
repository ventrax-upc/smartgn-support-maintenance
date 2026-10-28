package com.ventrax.smartgn.support.interfaces.rest.controllers;

import com.ventrax.smartgn.support.application.internal.commandservices.SupportCommandService;
import com.ventrax.smartgn.support.application.internal.queryservices.SupportQueryService;
import com.ventrax.smartgn.support.domain.model.entities.VisitDiagnosis;
import com.ventrax.smartgn.support.interfaces.rest.resources.SupportResources.CompleteVisitDiagnosisRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/support/visits/{visitId}/diagnosis")
public class DiagnosisController {

    private final SupportCommandService commandService;
    private final SupportQueryService queryService;

    public DiagnosisController(SupportCommandService commandService, SupportQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<VisitDiagnosis> completeVisit(@PathVariable UUID visitId, @RequestBody CompleteVisitDiagnosisRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.completeVisitWithDiagnosis(visitId, request.summary(), request.finalCost()));
    }

    @GetMapping
    public ResponseEntity<VisitDiagnosis> getDiagnosis(@PathVariable UUID visitId) {
        return queryService.getDiagnosisByVisit(visitId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}