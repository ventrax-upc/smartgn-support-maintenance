package com.ventrax.smartgn.support.domain.model.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "technician_assignments")
public class TechnicianAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "assignment_id")
    private UUID assignmentId;

    @Column(name = "visit_id", nullable = false)
    private UUID visitId;

    @Column(name = "technician_id", nullable = false)
    private UUID technicianId;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(nullable = false)
    private Boolean active = true;

    public TechnicianAssignment() {}

    public TechnicianAssignment(UUID visitId, UUID technicianId) {
        this.visitId = visitId;
        this.technicianId = technicianId;
        this.assignedAt = Instant.now();
        this.active = true;
    }

    public UUID getAssignmentId() { return assignmentId; }
    public UUID getVisitId() { return visitId; }
    public UUID getTechnicianId() { return technicianId; }
    public Instant getAssignedAt() { return assignedAt; }
    public Boolean getActive() { return active; }
}