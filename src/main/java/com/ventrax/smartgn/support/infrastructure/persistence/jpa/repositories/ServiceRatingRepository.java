package com.ventrax.smartgn.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.smartgn.support.domain.model.entities.ServiceRating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRatingRepository extends JpaRepository<ServiceRating, UUID> {
    Optional<ServiceRating> findByTicketId(UUID ticketId);
}