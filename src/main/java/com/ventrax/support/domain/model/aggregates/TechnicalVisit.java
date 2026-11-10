package com.ventrax.support.domain.model.aggregates;

import com.ventrax.support.domain.model.valueobjects.VisitStatus;

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

    @Transient
    private java.util.List<com.ventrax.support.domain.model.entities.TechnicianAssignment>
            assignments = java.util.List.of();

    public java.util.List<com.ventrax.support.domain.model.entities.TechnicianAssignment>
            getAssignments() {
        return assignments;
    }

    public void assignments(
            java.util.List<com.ventrax.support.domain.model.entities.TechnicianAssignment>
                    history) {
        assignments = java.util.List.copyOf(history);
    }

    public TechnicalVisit() {}

    public TechnicalVisit(UUID ticketId, Instant requestedWindowStart, Instant requestedWindowEnd) {
        this.ticketId = ticketId;
        this.requestedWindowStart = requestedWindowStart;
        this.requestedWindowEnd = requestedWindowEnd;
        this.status = VisitStatus.REQUESTED;
    }

    private Instant scheduledStart;
    private Instant scheduledEnd;
    private Instant startedAt;

    public Instant getScheduledStart() {
        return scheduledStart;
    }

    public Instant getScheduledEnd() {
        return scheduledEnd;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void schedule(Instant start, Instant end) {
        if (status != VisitStatus.ASSIGNED && status != VisitStatus.SCHEDULED)
            throw new IllegalStateException("Visit must be assigned");
        if (start == null
                || end == null
                || !start.isBefore(end)
                || start.isBefore(Instant.now())
                || start.isBefore(requestedWindowStart)
                || end.isAfter(requestedWindowEnd))
            throw new IllegalArgumentException("Invalid appointment window");
        scheduledStart = start;
        scheduledEnd = end;
        status = VisitStatus.SCHEDULED;
    }

    public void start() {
        if (status != VisitStatus.SCHEDULED)
            throw new IllegalStateException("Visit must be scheduled");
        startedAt = Instant.now();
        status = VisitStatus.IN_PROGRESS;
    }

    public void markAssigned() {
        if (status != VisitStatus.REQUESTED && status != VisitStatus.ASSIGNED)
            throw new IllegalStateException("Visit cannot be reassigned");
        this.status = VisitStatus.ASSIGNED;
    }

    public void completeService() {
        if (status != VisitStatus.IN_PROGRESS)
            throw new IllegalStateException("Visit must be in progress");
        this.status = VisitStatus.COMPLETED;
        this.completedAt = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
    }

    public UUID getVisitId() {
        return visitId;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public Instant getRequestedWindowStart() {
        return requestedWindowStart;
    }

    public Instant getRequestedWindowEnd() {
        return requestedWindowEnd;
    }

    public VisitStatus getStatus() {
        return status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
