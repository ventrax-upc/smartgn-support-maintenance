package com.ventrax.smartgn.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.smartgn.support.domain.model.entities.VisitDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface VisitDiagnosisRepository extends JpaRepository<VisitDiagnosis, UUID> {
    Optional<VisitDiagnosis> findByVisitId(UUID visitId);
}