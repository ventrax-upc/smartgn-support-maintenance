package com.ventrax.support.application.outbound.persistence;

import com.ventrax.support.domain.model.entities.TechnicianAssignment;

import java.util.*;

public interface TechnicianAssignmentRepositoryPort {
    Optional<TechnicianAssignment> findById(UUID id);

    List<TechnicianAssignment> findAll();

    <S extends TechnicianAssignment> S save(S entity);

    <S extends TechnicianAssignment> S saveAndFlush(S entity);

    List<TechnicianAssignment> findByVisitId(UUID visitId);

    java.util.List<TechnicianAssignment> findByVisitIdOrderByAssignedAtAsc(UUID visitId);
}
