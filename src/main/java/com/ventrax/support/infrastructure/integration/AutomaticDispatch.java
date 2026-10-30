package com.ventrax.support.infrastructure.integration;

import com.ventrax.support.application.internal.commandservices.SupportCommandService;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AutomaticDispatch {
    private final JdbcTemplate db;
    private final SupportCommandService service;

    public AutomaticDispatch(JdbcTemplate db, SupportCommandService service) {
        this.db = db;
        this.service = service;
    }

    @Scheduled(fixedDelay = 10000)
    public void dispatch() {
        for (var visit :
                db.queryForList(
                        "select visit_id from technical_visits where visit_status='REQUESTED' order"
                                + " by visit_id limit 20")) {
            var technicians =
                    db.queryForList(
                            "select technician_id from technicians where active and available order"
                                    + " by technician_id limit 1");
            if (technicians.isEmpty()) return;
            try {
                service.assignTechnician(
                        (UUID) visit.get("visit_id"),
                        (UUID) technicians.getFirst().get("technician_id"));
            } catch (org.springframework.dao.DataIntegrityViolationException
                    | IllegalStateException retry) {
            }
        }
    }
}
