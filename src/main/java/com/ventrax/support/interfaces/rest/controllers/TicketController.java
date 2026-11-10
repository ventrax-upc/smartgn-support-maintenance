package com.ventrax.support.interfaces.rest.controllers;

import com.ventrax.support.application.internal.commandservices.SupportCommandService;
import com.ventrax.support.application.internal.queryservices.SupportQueryService;
import com.ventrax.support.domain.model.aggregates.SupportTicket;
import com.ventrax.support.domain.model.entities.ServiceRating;
import com.ventrax.support.domain.model.entities.TicketPhoto;
import com.ventrax.support.domain.model.entities.TicketReply;
import com.ventrax.support.interfaces.rest.resources.SupportResources.*;

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

    @org.springframework.beans.factory.annotation.Autowired
    private com.ventrax.support.application.outbound.PhotoStore photos;

    private final SupportQueryService queryService;

    public TicketController(
            SupportCommandService commandService, SupportQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<SupportTicket> createTicket(
            @jakarta.validation.Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        commandService.createTicket(
                                request.principalAccountId(),
                                request.meterId(),
                                request.subject(),
                                request.description()));
    }

    @GetMapping
    public ResponseEntity<List<SupportTicket>> getTickets(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = queryService.tickets(status, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", Long.toString(result.getTotalElements()))
                .header("X-Page", Integer.toString(page))
                .header("X-Page-Size", Integer.toString(size))
                .body(result.getContent());
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<SupportTicket> getTicketById(@PathVariable UUID ticketId) {
        return queryService
                .getTicketById(ticketId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{ticketId}/replies")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<TicketReply> addReply(
            @PathVariable UUID ticketId,
            @jakarta.validation.Valid @RequestBody AddReplyRequest request) {
        var reply = commandService.addReply(ticketId, request.authorAccountId(), request.body());
        if (Boolean.TRUE.equals(request.resolve())) commandService.resolve(ticketId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    @GetMapping("/{ticketId}/replies")
    public ResponseEntity<List<TicketReply>> getReplies(
            @PathVariable UUID ticketId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return CollectionsPage.of(queryService.getRepliesByTicket(ticketId), page, size);
    }

    @PostMapping(value = "/{ticketId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketPhoto> addPhoto(
            @PathVariable UUID ticketId,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(required = false) String objectReference,
            @RequestParam(required = false) UUID uploadedByAccountId) {
        if (objectReference != null || file == null || file.isEmpty())
            throw new IllegalArgumentException(
                    "Photograph required; external references are not accepted");
        String ref = photos.save(file);
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(commandService.addPhoto(ticketId, ref, uploadedByAccountId));
        } catch (RuntimeException e) {
            photos.delete(ref);
            throw e;
        }
    }

    @GetMapping("/{ticketId}/photos/{photoId}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID ticketId, @PathVariable UUID photoId)
            throws java.io.IOException {
        var photo =
                queryService.getPhotosByTicket(ticketId).stream()
                        .filter(p -> p.getPhotoId().equals(photoId))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new org.springframework.web.server.ResponseStatusException(
                                                HttpStatus.NOT_FOUND));
        return ResponseEntity.ok()
                .header(
                        "Content-Type",
                        photo.getObjectReference().endsWith(".png") ? "image/png" : "image/jpeg")
                .header("X-Content-Type-Options", "nosniff")
                .body(photos.read(photo.getObjectReference()));
    }

    @GetMapping("/{ticketId}/photos")
    public ResponseEntity<List<TicketPhoto>> getPhotos(
            @PathVariable UUID ticketId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return CollectionsPage.of(queryService.getPhotosByTicket(ticketId), page, size);
    }

    @PostMapping("/{ticketId}/close")
    public ResponseEntity<SupportTicket> closeTicket(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(commandService.closeTicket(ticketId));
    }

    @PostMapping("/{ticketId}/rating")
    public ResponseEntity<ServiceRating> rateTicket(
            @PathVariable UUID ticketId,
            @jakarta.validation.Valid @RequestBody RateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.rateTicket(ticketId, request.score(), request.comment()));
    }

    @GetMapping("/{ticketId}/rating")
    public ResponseEntity<ServiceRating> getRating(@PathVariable UUID ticketId) {
        return queryService
                .getRatingByTicket(ticketId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
