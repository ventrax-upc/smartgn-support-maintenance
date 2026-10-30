package com.ventrax.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.support.domain.model.entities.TechnicianAssignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TechnicianAssignmentRepository
        extends JpaRepository<TechnicianAssignment, UUID>,
                com.ventrax.support.application.outbound.persistence
                        .TechnicianAssignmentRepositoryPort {
    List<TechnicianAssignment> findByVisitId(UUID visitId);

    java.util.List<TechnicianAssignment> findByVisitIdOrderByAssignedAtAsc(UUID visitId);
}
