package com.ventrax.support.application.outbound.persistence;

import com.ventrax.support.domain.model.entities.VisitDiagnosis;

import java.util.*;

public interface VisitDiagnosisRepositoryPort {
    Optional<VisitDiagnosis> findById(UUID id);

    List<VisitDiagnosis> findAll();

    <S extends VisitDiagnosis> S save(S entity);

    <S extends VisitDiagnosis> S saveAndFlush(S entity);

    Optional<VisitDiagnosis> findByVisitId(UUID visitId);
}
