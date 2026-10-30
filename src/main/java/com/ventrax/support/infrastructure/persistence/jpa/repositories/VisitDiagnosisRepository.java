package com.ventrax.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.support.domain.model.entities.VisitDiagnosis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VisitDiagnosisRepository
        extends JpaRepository<VisitDiagnosis, UUID>,
                com.ventrax.support.application.outbound.persistence.VisitDiagnosisRepositoryPort {
    Optional<VisitDiagnosis> findByVisitId(UUID visitId);
}
