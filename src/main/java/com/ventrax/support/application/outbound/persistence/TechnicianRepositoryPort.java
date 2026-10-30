package com.ventrax.support.application.outbound.persistence;

import com.ventrax.support.domain.model.entities.Technician;

import java.util.*;

public interface TechnicianRepositoryPort {
    Optional<Technician> findById(UUID id);

    List<Technician> findAll();

    <S extends Technician> S save(S entity);

    <S extends Technician> S saveAndFlush(S entity);

    List<Technician> findByAvailableTrueAndActiveTrue();
}
