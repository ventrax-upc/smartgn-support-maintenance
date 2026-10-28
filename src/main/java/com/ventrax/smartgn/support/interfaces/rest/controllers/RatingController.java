package com.ventrax.smartgn.support.interfaces.rest.controllers;

import com.ventrax.smartgn.support.application.internal.commandservices.SupportCommandService;
import com.ventrax.smartgn.support.application.internal.queryservices.SupportQueryService;
import com.ventrax.smartgn.support.domain.model.entities.ServiceRating;
import com.ventrax.smartgn.support.interfaces.rest.resources.SupportResources.RateTicketRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/support/tickets/{ticketId}/rating")
public class RatingController {

    private final SupportCommandService commandService;
    private final SupportQueryService queryService;

    public RatingController(SupportCommandService commandService, SupportQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<ServiceRating> rateTicket(@PathVariable UUID ticketId, @RequestBody RateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.rateTicket(ticketId, request.score(), request.comment()));
    }

    @GetMapping
    public ResponseEntity<ServiceRating> getRating(@PathVariable UUID ticketId) {
        return queryService.getRatingByTicket(ticketId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}