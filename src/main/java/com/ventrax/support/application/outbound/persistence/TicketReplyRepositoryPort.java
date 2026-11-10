package com.ventrax.support.application.outbound.persistence;

import com.ventrax.support.domain.model.entities.TicketReply;

import java.util.*;

public interface TicketReplyRepositoryPort {
    Optional<TicketReply> findById(UUID id);

    List<TicketReply> findAll();

    <S extends TicketReply> S save(S entity);

    <S extends TicketReply> S saveAndFlush(S entity);

    List<TicketReply> findByTicketIdOrderByPostedAtAsc(UUID ticketId);
}
