package com.ventrax.smartgn.support.interfaces.rest.controllers;

import com.ventrax.smartgn.support.application.internal.commandservices.SupportCommandService;
import com.ventrax.smartgn.support.application.internal.queryservices.SupportQueryService;
import com.ventrax.smartgn.support.domain.model.entities.TicketPhoto;
import com.ventrax.smartgn.support.interfaces.rest.resources.SupportResources.AddPhotoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/support/tickets/{ticketId}/photos")
public class PhotoController {

    private final SupportCommandService commandService;
    private final SupportQueryService queryService;

    public PhotoController(SupportCommandService commandService, SupportQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<TicketPhoto> addPhoto(@PathVariable UUID ticketId, @RequestBody AddPhotoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.addPhoto(ticketId, request.objectReference(), request.uploadedByAccountId()));
    }

    @GetMapping
    public ResponseEntity<List<TicketPhoto>> getPhotos(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(queryService.getPhotosByTicket(ticketId));
    }
}