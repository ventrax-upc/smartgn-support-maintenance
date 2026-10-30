package com.ventrax.support.application.outbound.persistence;

import com.ventrax.support.domain.model.aggregates.TechnicalVisit;

import java.util.*;

public interface TechnicalVisitRepositoryPort {
    Optional<TechnicalVisit> findById(UUID id);

    List<TechnicalVisit> findAll();

    <S extends TechnicalVisit> S save(S entity);

    <S extends TechnicalVisit> S saveAndFlush(S entity);

    List<TechnicalVisit> findByTicketId(UUID ticketId);
}
