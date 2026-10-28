package com.ventrax.smartgn.support.infrastructure.persistence.jpa.repositories;

import com.ventrax.smartgn.support.domain.model.entities.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, UUID> {
    List<KnowledgeArticle> findByActiveTrue();
}