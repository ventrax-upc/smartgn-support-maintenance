package com.ventrax.support.domain.model.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_ratings")
public class ServiceRating {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rating_id")
    private UUID ratingId;

    @Column(name = "ticket_id", nullable = false, unique = true)
    private UUID ticketId;

    @Column(nullable = false)
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "rated_at", nullable = false)
    private Instant ratedAt;

    public ServiceRating() {}

    public ServiceRating(UUID ticketId, Integer score, String comment) {
        this.ticketId = ticketId;
        this.score = score;
        this.comment = comment;
        this.ratedAt = Instant.now();
    }

    public UUID getRatingId() {
        return ratingId;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public Integer getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }

    public Instant getRatedAt() {
        return ratedAt;
    }
}
