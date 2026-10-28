package com.ventrax.smartgn.support.domain.model.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_replies")
public class TicketReply {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reply_id")
    private UUID replyId;

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "author_account_id", nullable = false)
    private UUID authorAccountId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "posted_at", nullable = false)
    private Instant postedAt;

    public TicketReply() {}

    public TicketReply(UUID ticketId, UUID authorAccountId, String body) {
        this.ticketId = ticketId;
        this.authorAccountId = authorAccountId;
        this.body = body;
        this.postedAt = Instant.now();
    }

    public UUID getReplyId() { return replyId; }
    public UUID getTicketId() { return ticketId; }
    public UUID getAuthorAccountId() { return authorAccountId; }
    public String getBody() { return body; }
    public Instant getPostedAt() { return postedAt; }
}