package com.ventrax.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.support.domain.model.entities.TicketPhoto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketPhotoRepository
        extends JpaRepository<TicketPhoto, UUID>,
                com.ventrax.support.application.outbound.persistence.TicketPhotoRepositoryPort {
    List<TicketPhoto> findByTicketId(UUID ticketId);
}
