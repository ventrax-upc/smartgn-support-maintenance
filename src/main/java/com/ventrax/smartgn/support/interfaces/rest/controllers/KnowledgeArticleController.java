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
@RequestMapping("/api/v1/support/articles")
public class KnowledgeArticleController {

    private final SupportCommandService commandService;
    private final SupportQueryService queryService;

    public KnowledgeArticleController(SupportCommandService commandService, SupportQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<KnowledgeArticle> createArticle(@RequestBody CreateArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commandService.createArticle(request.title(), request.category(), request.contentReference()));
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeArticle>> getArticles() {
        return ResponseEntity.ok(queryService.getActiveArticles());
    }
}