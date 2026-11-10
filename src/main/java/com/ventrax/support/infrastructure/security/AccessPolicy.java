package com.ventrax.support.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Component
public class AccessPolicy implements com.ventrax.support.application.outbound.AccessPort {
    private final JdbcTemplate db;
    private final RestClient iam, installation;

    public AccessPolicy(
            JdbcTemplate db,
            @Value("${smartgn.iam-url}") String i,
            @Value("${smartgn.installation-url}") String m,
            @Value("${smartgn.internal-token}") String token) {
        this.db = db;
        var f =
                new org.springframework.http.client.JdkClientHttpRequestFactory(
                        java.net.http.HttpClient.newBuilder()
                                .connectTimeout(java.time.Duration.ofSeconds(2))
                                .build());
        f.setReadTimeout(java.time.Duration.ofSeconds(3));
        iam =
                RestClient.builder()
                        .baseUrl(i)
                        .requestFactory(f)
                        .defaultHeader("Authorization", "Bearer " + token)
                        .build();
        installation =
                RestClient.builder()
                        .baseUrl(m)
                        .requestFactory(f)
                        .defaultHeader("Authorization", "Bearer " + token)
                        .build();
    }

    public java.util.Set<UUID> permittedMeters(String function) {
        var principal = actor().get("principalAccountId");
        if (principal == null) principal = actor().get("accountId");
        java.util.Set<UUID> ids = new java.util.HashSet<>();
        for (var row :
                db.queryForList(
                        "select distinct meter_id from support_tickets where"
                                + " principal_account_id=?",
                        UUID.fromString(principal.toString()))) {
            UUID id = (UUID) row.get("meter_id");
            try {
                meter(id, function);
                ids.add(id);
            } catch (ResponseStatusException e) {
                if (e.getStatusCode().value() != 403) throw e;
            }
        }
        return ids;
    }

    public Map actor() {
        return (Map)
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();
    }

    public UUID actorId() {
        if (org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .getAuthentication()
                == null) return null;
        return UUID.fromString(actor().get("accountId").toString());
    }

    public void admin() {
        if (!"SUPERADMIN".equals(actor().get("role")))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    public UUID meter(UUID meter, String function) {
        Map owner =
                installation
                        .get()
                        .uri("/internal/v1/meters/{id}/ownership", meter)
                        .retrieve()
                        .onStatus(
                                status -> status.value() == 404,
                                (request, response) -> {
                                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                                })
                        .body(Map.class);
        if (owner == null || !Boolean.TRUE.equals(owner.get("active")))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (!"SUPERADMIN".equals(actor().get("role"))) {
            Map allowed =
                    iam.post()
                            .uri("/internal/v1/authorizations/check")
                            .body(
                                    Map.of(
                                            "accountId",
                                            actorId(),
                                            "meterId",
                                            meter,
                                            "function",
                                            function,
                                            "requiresPro",
                                            false))
                            .retrieve()
                            .body(Map.class);
            if (allowed == null || !Boolean.TRUE.equals(allowed.get("allowed")))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return UUID.fromString(owner.get("ownerAccountId").toString());
    }

    public void identity(UUID supplied, UUID actual) {
        if (supplied != null && !supplied.equals(actual))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    public void checkRoute(HttpServletRequest q, Map actor) {
        String p = q.getRequestURI();
        boolean write = !q.getMethod().equals("GET");
        if (write
                && (p.contains("/technicians")
                        || p.contains("/knowledge-articles")
                        || p.matches(".*/visits/[^/]+/(assign|schedule|start|complete)"))) admin();
        String[] parts = p.split("/");
        UUID ticket = null;
        if (parts.length > 5 && parts[4].equals("tickets")) ticket = UUID.fromString(parts[5]);
        if (parts.length > 5 && parts[4].equals("visits")) {
            var rows =
                    db.queryForList(
                            "select ticket_id from technical_visits where visit_id=?",
                            UUID.fromString(parts[5]));
            if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            ticket = (UUID) rows.getFirst().get("ticket_id");
        }
        if (ticket != null) {
            var rows = db.queryForList("select * from support_tickets where ticket_id=?", ticket);
            if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            var t = rows.getFirst();
            boolean ownerOnly = p.endsWith("/close") || p.endsWith("/rating") && write;
            if (!"SUPERADMIN".equals(actor.get("role"))) {
                UUID owner =
                        meter(
                                (UUID) t.get("meter_id"),
                                p.contains("/visits") ? "USE_VISITS" : "USE_TICKETS");
                if (!owner.equals(t.get("principal_account_id")))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            if (ownerOnly
                    && (!"PRINCIPAL".equals(actor.get("accountKind"))
                            || !actorId().equals(t.get("principal_account_id"))))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
