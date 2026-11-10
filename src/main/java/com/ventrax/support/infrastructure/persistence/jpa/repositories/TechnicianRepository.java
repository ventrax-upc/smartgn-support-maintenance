package com.ventrax.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.support.domain.model.entities.Technician;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TechnicianRepository
        extends JpaRepository<Technician, UUID>,
                com.ventrax.support.application.outbound.persistence.TechnicianRepositoryPort {
    List<Technician> findByAvailableTrueAndActiveTrue();
}
