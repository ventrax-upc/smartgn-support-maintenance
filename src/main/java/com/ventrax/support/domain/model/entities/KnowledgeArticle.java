package com.ventrax.support.domain.model.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_articles")
public class KnowledgeArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "article_id")
    private UUID articleId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(name = "content_reference", nullable = false)
    private String contentReference;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(nullable = false)
    private Boolean active = true;

    public KnowledgeArticle() {}

    public KnowledgeArticle(String title, String category, String contentReference) {
        this.title = title;
        this.category = category;
        this.contentReference = contentReference;
        this.publishedAt = Instant.now();
        this.active = true;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getContentReference() {
        return contentReference;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Boolean getActive() {
        return active;
    }
}
