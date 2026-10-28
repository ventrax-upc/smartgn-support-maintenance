package com.ventrax.smartgn.support.interfaces.rest.controllers;

import com.ventrax.smartgn.support.application.internal.commandservices.SupportCommandService;
import com.ventrax.smartgn.support.application.internal.queryservices.SupportQueryService;
import com.ventrax.smartgn.support.domain.model.aggregates.SupportTicket;
import com.ventrax.smartgn.support.domain.model.entities.TicketReply;
import com.ventrax.smartgn.support.interfaces.rest.resources.SupportResources.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/support/tickets")
public class TicketController {

    private final SupportCommandService commandService;
    private final SupportQueryService queryService;

    public TicketController(SupportCommandService commandService, SupportQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<SupportTicket> createTicket(@RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.createTicket(request.principalAccountId(), request.meterId(), request.subject(), request.message()));
    }

    @GetMapping
    public ResponseEntity<List<SupportTicket>> getTickets() {
        return ResponseEntity.ok(queryService.getAllTickets());
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<SupportTicket> getTicketById(@PathVariable UUID ticketId) {
        return queryService.getTicketById(ticketId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{ticketId}/replies")
    public ResponseEntity<TicketReply> addReply(@PathVariable UUID ticketId, @RequestBody AddReplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.addReply(ticketId, request.authorAccountId(), request.body()));
    }

    @GetMapping("/{ticketId}/replies")
    public ResponseEntity<List<TicketReply>> getReplies(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(queryService.getRepliesByTicket(ticketId));
    }

    @PutMapping("/{ticketId}/close")
    public ResponseEntity<SupportTicket> closeTicket(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(commandService.closeTicket(ticketId));
    }
}