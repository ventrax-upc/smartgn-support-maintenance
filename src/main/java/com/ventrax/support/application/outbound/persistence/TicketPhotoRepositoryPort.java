package com.ventrax.support.application.outbound.persistence;

import com.ventrax.support.domain.model.entities.TicketPhoto;

import java.util.*;

public interface TicketPhotoRepositoryPort {
    Optional<TicketPhoto> findById(UUID id);

    List<TicketPhoto> findAll();

    <S extends TicketPhoto> S save(S entity);

    <S extends TicketPhoto> S saveAndFlush(S entity);

    List<TicketPhoto> findByTicketId(UUID ticketId);
}
