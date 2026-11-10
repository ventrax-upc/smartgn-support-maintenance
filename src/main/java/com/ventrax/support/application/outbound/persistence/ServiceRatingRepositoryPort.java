package com.ventrax.support.application.outbound.persistence;

import com.ventrax.support.domain.model.entities.ServiceRating;

import java.util.*;

public interface ServiceRatingRepositoryPort {
    Optional<ServiceRating> findById(UUID id);

    List<ServiceRating> findAll();

    <S extends ServiceRating> S save(S entity);

    <S extends ServiceRating> S saveAndFlush(S entity);

    Optional<ServiceRating> findByTicketId(UUID ticketId);
}
