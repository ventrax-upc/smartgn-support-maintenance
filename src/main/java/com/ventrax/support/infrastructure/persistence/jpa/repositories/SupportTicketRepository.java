package com.ventrax.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.support.domain.model.aggregates.SupportTicket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, UUID>,
                com.ventrax.support.application.outbound.persistence.SupportTicketRepositoryPort {
    @org.springframework.data.jpa.repository.Query(
            "select t from SupportTicket t where (:admin=true or (t.principalAccountId=:principal"
                    + " and t.meterId in :meters)) and (:status is null or t.status=:status)")
    org.springframework.data.domain.Page<SupportTicket> accessible(
            boolean admin,
            UUID principal,
            java.util.Collection<UUID> meters,
            com.ventrax.support.domain.model.valueobjects.TicketStatus status,
            org.springframework.data.domain.Pageable page);

    List<SupportTicket> findByPrincipalAccountId(UUID principalAccountId);
}
