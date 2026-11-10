package com.ventrax.support.application.internal.commandservices;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CoverageService {
    private final JdbcTemplate db;

    public CoverageService(JdbcTemplate db) {
        this.db = db;
    }

    @Transactional
    public void receive(Map<String, Object> event) {
        if (!"CoverageCalculated".equals(event.get("event_type")))
            throw new IllegalArgumentException("Unsupported event");
        if (event.get("event_id") == null
                || event.get("aggregate_id") == null
                || !(event.get("payload") instanceof Map))
            throw new IllegalArgumentException("Invalid event envelope");
        UUID id = UUID.fromString(event.get("event_id").toString());
        UUID visit = UUID.fromString(event.get("aggregate_id").toString());
        Map p = (Map) event.get("payload");
        if (p.get("visitId") == null || p.get("accountId") == null || p.get("cost") == null)
            throw new IllegalArgumentException("Incomplete coverage evidence");
        if (!visit.toString().equals(p.get("visitId").toString()))
            throw new IllegalArgumentException("Visit mismatch");
        var ticket =
                db.queryForList(
                        "select t.principal_account_id,d.final_cost from support_tickets t join"
                            + " technical_visits v using(ticket_id) join visit_diagnoses d"
                            + " using(visit_id) where v.visit_id=? and v.visit_status='COMPLETED'",
                        visit);
        if (ticket.isEmpty()
                || !ticket.getFirst()
                        .get("principal_account_id")
                        .toString()
                        .equals(p.get("accountId").toString()))
            throw new IllegalArgumentException("Account mismatch");
        java.math.BigDecimal cost;
        try {
            cost = new java.math.BigDecimal(p.get("cost").toString());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid coverage cost");
        }
        if (cost.compareTo((java.math.BigDecimal) ticket.getFirst().get("final_cost")) != 0)
            throw new IllegalStateException("Coverage cost differs from completed diagnosis");
        var canonical = new TreeMap<String, Object>(p);
        for (String field : List.of("cost", "balance")) {
            if (canonical.get(field) != null)
                canonical.put(
                        field,
                        new java.math.BigDecimal(canonical.get(field).toString())
                                .stripTrailingZeros()
                                .toPlainString());
        }
        String payload = new tools.jackson.databind.ObjectMapper().writeValueAsString(canonical);
        var old =
                db.queryForList(
                        "select payload from support_inbox where id=? or visit_id=?", id, visit);
        if (!old.isEmpty()) {
            var previous =
                    new tools.jackson.databind.ObjectMapper()
                            .readValue(old.getFirst().get("payload").toString(), Map.class);
            var normalized = new TreeMap<String, Object>(previous);
            for (String field : List.of("cost", "balance")) {
                if (normalized.get(field) != null)
                    normalized.put(
                            field,
                            new java.math.BigDecimal(normalized.get(field).toString())
                                    .stripTrailingZeros()
                                    .toPlainString());
            }
            if (!payload.equals(
                    new tools.jackson.databind.ObjectMapper().writeValueAsString(normalized)))
                throw new IllegalStateException("Conflicting event");
            return;
        }
        db.update(
                "insert into support_inbox(id,payload,visit_id) values(?,?,?)", id, payload, visit);
        if (p.get("orderId") != null)
            db.update(
                    "update visit_diagnoses set billing_reference_id=? where visit_id=?",
                    UUID.fromString(p.get("orderId").toString()),
                    visit);
    }
}
