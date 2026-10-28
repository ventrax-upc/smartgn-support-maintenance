package com.ventrax.smartgn.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.smartgn.support.domain.model.entities.TechnicianAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TechnicianAssignmentRepository extends JpaRepository<TechnicianAssignment, UUID> {
    List<TechnicianAssignment> findByVisitId(UUID visitId);
}