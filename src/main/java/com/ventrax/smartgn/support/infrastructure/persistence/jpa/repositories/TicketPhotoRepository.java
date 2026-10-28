package com.ventrax.smartgn.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.smartgn.support.domain.model.entities.TicketPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TicketPhotoRepository extends JpaRepository<TicketPhoto, UUID> {
    List<TicketPhoto> findByTicketId(UUID ticketId);
}