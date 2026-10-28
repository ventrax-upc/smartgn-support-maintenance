package com.ventrax.smartgn.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.smartgn.support.domain.model.aggregates.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {
    List<SupportTicket> findByPrincipalAccountId(UUID principalAccountId);
}