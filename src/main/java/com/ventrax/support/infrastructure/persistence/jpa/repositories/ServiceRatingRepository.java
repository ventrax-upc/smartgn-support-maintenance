package com.ventrax.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.support.domain.model.entities.ServiceRating;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceRatingRepository
        extends JpaRepository<ServiceRating, UUID>,
                com.ventrax.support.application.outbound.persistence.ServiceRatingRepositoryPort {
    Optional<ServiceRating> findByTicketId(UUID ticketId);
}
