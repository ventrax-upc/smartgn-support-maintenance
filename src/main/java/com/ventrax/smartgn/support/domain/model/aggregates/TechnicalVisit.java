package com.ventrax.smartgn.support.domain.model.aggregates;

import com.ventrax.smartgn.support.domain.model.valueobjects.VisitStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "technical_visits")
public class TechnicalVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "visit_id")
    private UUID visitId;

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "requested_window_start")
    private Instant requestedWindowStart;

    @Column(name = "requested_window_end")
    private Instant requestedWindowEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_status", nullable = false, length = 30)
    private VisitStatus status;

    @Column(name = "completed_at")
    private Instant completedAt;

    public TechnicalVisit() {}

    public TechnicalVisit(UUID ticketId, Instant requestedWindowStart, Instant requestedWindowEnd) {
        this.ticketId = ticketId;
        this.requestedWindowStart = requestedWindowStart;
        this.requestedWindowEnd = requestedWindowEnd;
        this.status = VisitStatus.REQUESTED;
    }

    public void markAssigned() {
        this.status = VisitStatus.ASSIGNED;
    }

    public void completeService() {
        this.status = VisitStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public UUID getVisitId() { return visitId; }
    public UUID getTicketId() { return ticketId; }
    public Instant getRequestedWindowStart() { return requestedWindowStart; }
    public Instant getRequestedWindowEnd() { return requestedWindowEnd; }
    public VisitStatus getStatus() { return status; }
    public Instant getCompletedAt() { return completedAt; }
}