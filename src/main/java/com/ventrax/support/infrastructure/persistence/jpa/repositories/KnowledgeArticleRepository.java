package com.ventrax.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.support.domain.model.entities.KnowledgeArticle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KnowledgeArticleRepository
        extends JpaRepository<KnowledgeArticle, UUID>,
                com.ventrax.support.application.outbound.persistence
                        .KnowledgeArticleRepositoryPort {
    List<KnowledgeArticle> findByActiveTrue();
}
