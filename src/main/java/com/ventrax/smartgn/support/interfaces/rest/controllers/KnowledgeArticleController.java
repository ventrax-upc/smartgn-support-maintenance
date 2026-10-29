package com.ventrax.smartgn.support.interfaces.rest.controllers;

import com.ventrax.smartgn.support.application.internal.commandservices.SupportCommandService;
import com.ventrax.smartgn.support.application.internal.queryservices.SupportQueryService;
import com.ventrax.smartgn.support.domain.model.entities.KnowledgeArticle;
import com.ventrax.smartgn.support.interfaces.rest.resources.SupportResources.CreateArticleRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support/knowledge-articles")
public class KnowledgeArticleController {

    private final SupportCommandService commandService;
    private final SupportQueryService queryService;

    public KnowledgeArticleController(SupportCommandService commandService, SupportQueryService queryService) {
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
            articles = articles.stream().filter(a -> a.getCategory().equalsIgnoreCase(category)).toList();
        }
        return ResponseEntity.ok(articles);
    }

    @PostMapping
    public ResponseEntity<KnowledgeArticle> createArticle(@RequestBody CreateArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.createArticle(request.title(), request.category(), request.contentReference()));
    }
}