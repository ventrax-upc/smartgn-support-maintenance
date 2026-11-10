package com.ventrax.support.application.outbound.persistence;

import com.ventrax.support.domain.model.aggregates.SupportTicket;

import java.util.*;

public interface SupportTicketRepositoryPort {
    Optional<SupportTicket> findById(UUID id);

    List<SupportTicket> findAll();

    <S extends SupportTicket> S save(S entity);

    <S extends SupportTicket> S saveAndFlush(S entity);

    org.springframework.data.domain.Page<SupportTicket> accessible(
            boolean admin,
            UUID principal,
            java.util.Collection<UUID> meters,
            com.ventrax.support.domain.model.valueobjects.TicketStatus status,
            org.springframework.data.domain.Pageable page);

    List<SupportTicket> findByPrincipalAccountId(UUID principalAccountId);
}
