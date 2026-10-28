package com.ventrax.smartgn.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.smartgn.support.domain.model.aggregates.TechnicalVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TechnicalVisitRepository extends JpaRepository<TechnicalVisit, UUID> {
    List<TechnicalVisit> findByTicketId(UUID ticketId);
}