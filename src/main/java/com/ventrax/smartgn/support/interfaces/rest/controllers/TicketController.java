package com.ventrax.smartgn.support.interfaces.rest.controllers;

import com.ventrax.smartgn.support.application.internal.commandservices.SupportCommandService;
import com.ventrax.smartgn.support.application.internal.queryservices.SupportQueryService;
import com.ventrax.smartgn.support.domain.model.aggregates.SupportTicket;
import com.ventrax.smartgn.support.domain.model.entities.ServiceRating;
import com.ventrax.smartgn.support.domain.model.entities.TicketPhoto;
import com.ventrax.smartgn.support.domain.model.entities.TicketReply;
import com.ventrax.smartgn.support.interfaces.rest.resources.SupportResources.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
                .body(commandService.createTicket(request.principalAccountId(), request.meterId(), request.subject(), request.description()));
    }

    @GetMapping
    public ResponseEntity<List<SupportTicket>> getTickets(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
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

    @PostMapping(value = "/{ticketId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketPhoto> addPhoto(
            @PathVariable UUID ticketId,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(defaultValue = "s3://smartgn-support/evidencia-medidor.jpg") String objectReference,
            @RequestParam(required = false) UUID uploadedByAccountId) {
        String ref = (file != null && !file.isEmpty())
                ? "s3://smartgn-support/" + file.getOriginalFilename()
                : objectReference;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.addPhoto(ticketId, ref, uploadedByAccountId));
    }

    @GetMapping("/{ticketId}/photos")
    public ResponseEntity<List<TicketPhoto>> getPhotos(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(queryService.getPhotosByTicket(ticketId));
    }

    @PostMapping("/{ticketId}/close")
    public ResponseEntity<SupportTicket> closeTicket(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(commandService.closeTicket(ticketId));
    }

    @PostMapping("/{ticketId}/rating")
    public ResponseEntity<ServiceRating> rateTicket(@PathVariable UUID ticketId, @RequestBody RateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.rateTicket(ticketId, request.score(), request.comment()));
    }

    @GetMapping("/{ticketId}/rating")
    public ResponseEntity<ServiceRating> getRating(@PathVariable UUID ticketId) {
        return queryService.getRatingByTicket(ticketId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}