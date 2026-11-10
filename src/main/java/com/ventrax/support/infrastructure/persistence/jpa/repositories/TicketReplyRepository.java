package com.ventrax.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.support.domain.model.entities.TicketReply;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketReplyRepository
        extends JpaRepository<TicketReply, UUID>,
                com.ventrax.support.application.outbound.persistence.TicketReplyRepositoryPort {
    List<TicketReply> findByTicketIdOrderByPostedAtAsc(UUID ticketId);
}
