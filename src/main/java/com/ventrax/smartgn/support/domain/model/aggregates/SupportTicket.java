package com.ventrax.smartgn.support.domain.model.aggregates;

import com.ventrax.smartgn.support.domain.model.valueobjects.TicketStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "principal_account_id", nullable = false)
    private UUID principalAccountId;

    @Column(name = "meter_id", nullable = false)
    private UUID meterId;

    @Column(nullable = false, length = 180)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = false, length = 30)
    private TicketStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    public SupportTicket() {}

    public SupportTicket(UUID principalAccountId, UUID meterId, String subject, String message) {
        this.principalAccountId = principalAccountId;
        this.meterId = meterId;
        this.subject = subject;
        this.message = message;
        this.status = TicketStatus.OPEN;
        this.createdAt = Instant.now();
    }

    public void requestVisit() {
        if (this.status == TicketStatus.CLOSED) {
            throw new IllegalStateException("No se puede solicitar visita en un ticket cerrado.");
        }
        this.status = TicketStatus.VISIT_REQUESTED;
    }

    public void markResolved() {
        this.status = TicketStatus.RESOLVED;
        this.resolvedAt = Instant.now();
    }

    public void close() {
        this.status = TicketStatus.CLOSED;
        this.closedAt = Instant.now();
        if (this.resolvedAt == null) {
            this.resolvedAt = this.closedAt;
        }
    }

    public UUID getTicketId() { return ticketId; }
    public UUID getPrincipalAccountId() { return principalAccountId; }
    public UUID getMeterId() { return meterId; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public TicketStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public Instant getClosedAt() { return closedAt; }
}