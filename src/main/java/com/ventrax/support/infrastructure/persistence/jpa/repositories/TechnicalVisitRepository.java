package com.ventrax.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.support.domain.model.aggregates.TechnicalVisit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TechnicalVisitRepository
        extends JpaRepository<TechnicalVisit, UUID>,
                com.ventrax.support.application.outbound.persistence.TechnicalVisitRepositoryPort {
    List<TechnicalVisit> findByTicketId(UUID ticketId);
}
