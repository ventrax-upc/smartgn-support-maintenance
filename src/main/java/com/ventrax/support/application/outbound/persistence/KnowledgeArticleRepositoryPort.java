package com.ventrax.support.application.outbound.persistence;

import com.ventrax.support.domain.model.entities.KnowledgeArticle;

import java.util.*;

public interface KnowledgeArticleRepositoryPort {
    Optional<KnowledgeArticle> findById(UUID id);

    List<KnowledgeArticle> findAll();

    <S extends KnowledgeArticle> S save(S entity);

    <S extends KnowledgeArticle> S saveAndFlush(S entity);

    List<KnowledgeArticle> findByActiveTrue();
}
