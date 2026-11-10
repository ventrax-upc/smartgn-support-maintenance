package com.ventrax.support.interfaces.rest.controllers;

import com.ventrax.support.application.internal.commandservices.SupportCommandService;
import com.ventrax.support.application.internal.queryservices.SupportQueryService;
import com.ventrax.support.domain.model.entities.KnowledgeArticle;
import com.ventrax.support.interfaces.rest.resources.SupportResources.CreateArticleRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support/knowledge-articles")
public class KnowledgeArticleController {

    private final SupportCommandService commandService;
    private final SupportQueryService queryService;

    public KnowledgeArticleController(
            SupportCommandService commandService, SupportQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeArticle>> getArticles(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<KnowledgeArticle> articles = queryService.getActiveArticles();
        if (category != null && !category.isBlank()) {
            articles =
                    articles.stream()
                            .filter(a -> a.getCategory().equalsIgnoreCase(category))
                            .toList();
        }
        if (page < 0 || size < 1 || size > 100)
            throw new IllegalArgumentException("Invalid pagination");
        return ResponseEntity.ok()
                .header("X-Total-Count", Integer.toString(articles.size()))
                .body(
                        articles.stream()
                                .sorted(
                                        java.util.Comparator.comparing(
                                                KnowledgeArticle::getArticleId))
                                .skip((long) page * size)
                                .limit(size)
                                .toList());
    }

    @PostMapping
    public ResponseEntity<KnowledgeArticle> createArticle(
            @jakarta.validation.Valid @RequestBody CreateArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        commandService.createArticle(
                                request.title(), request.category(), request.contentReference()));
    }
}
