package com.ventrax.smartgn.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.smartgn.support.domain.model.entities.TicketReply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TicketReplyRepository extends JpaRepository<TicketReply, UUID> {
    List<TicketReply> findByTicketIdOrderByPostedAtAsc(UUID ticketId);
}