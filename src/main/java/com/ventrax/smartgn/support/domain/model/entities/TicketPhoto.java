package com.ventrax.smartgn.support.domain.model.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_photos")
public class TicketPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "photo_id")
    private UUID photoId;

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "object_reference", nullable = false)
    private String objectReference;

    @Column(name = "uploaded_by_account_id", nullable = false)
    private UUID uploadedByAccountId;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    public TicketPhoto() {}

    public TicketPhoto(UUID ticketId, String objectReference, UUID uploadedByAccountId) {
        this.ticketId = ticketId;
        this.objectReference = objectReference;
        this.uploadedByAccountId = uploadedByAccountId;
        this.uploadedAt = Instant.now();
    }

    public UUID getPhotoId() { return photoId; }
    public UUID getTicketId() { return ticketId; }
    public String getObjectReference() { return objectReference; }
    public UUID getUploadedByAccountId() { return uploadedByAccountId; }
    public Instant getUploadedAt() { return uploadedAt; }
}