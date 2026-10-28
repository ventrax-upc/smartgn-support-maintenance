package com.ventrax.smartgn.support.domain.model.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "visit_diagnoses")
public class VisitDiagnosis {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "diagnosis_id")
    private UUID diagnosisId;

    @Column(name = "visit_id", nullable = false, unique = true)
    private UUID visitId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "final_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalCost;

    @Column(name = "billing_reference_id")
    private UUID billingReferenceId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public VisitDiagnosis() {}

    public VisitDiagnosis(UUID visitId, String summary, BigDecimal finalCost, UUID billingReferenceId) {
        this.visitId = visitId;
        this.summary = summary;
        this.finalCost = finalCost;
        this.billingReferenceId = billingReferenceId;
        this.createdAt = Instant.now();
    }

    public UUID getDiagnosisId() { return diagnosisId; }
    public UUID getVisitId() { return visitId; }
    public String getSummary() { return summary; }
    public BigDecimal getFinalCost() { return finalCost; }
    public UUID getBillingReferenceId() { return billingReferenceId; }
    public Instant getCreatedAt() { return createdAt; }
}