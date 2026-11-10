package com.ventrax.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ventrax.support.application.internal.commandservices.CoverageService;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.*;

class CoverageServiceTest {
    @Test
    void rejectsCostMismatchBeforeWriting() {
        var db = mock(JdbcTemplate.class);
        UUID visit = UUID.randomUUID(), account = UUID.randomUUID();
        when(db.queryForList(contains("support_tickets"), eq(visit)))
                .thenReturn(
                        List.of(
                                Map.of(
                                        "principal_account_id",
                                        account,
                                        "final_cost",
                                        new BigDecimal("20.00"))));
        Map<String, Object> event =
                Map.of(
                        "event_id",
                        UUID.randomUUID().toString(),
                        "event_type",
                        "CoverageCalculated",
                        "aggregate_id",
                        visit.toString(),
                        "payload",
                        Map.of(
                                "visitId",
                                visit.toString(),
                                "accountId",
                                account.toString(),
                                "cost",
                                21));
        assertThrows(IllegalStateException.class, () -> new CoverageService(db).receive(event));
        verify(db, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void rejectsUnknownVisit() {
        var db = mock(JdbcTemplate.class);
        UUID visit = UUID.randomUUID();
        Map<String, Object> event =
                Map.of(
                        "event_id",
                        UUID.randomUUID().toString(),
                        "event_type",
                        "CoverageCalculated",
                        "aggregate_id",
                        visit.toString(),
                        "payload",
                        Map.of(
                                "visitId",
                                visit.toString(),
                                "accountId",
                                UUID.randomUUID().toString(),
                                "cost",
                                20));
        assertThrows(IllegalArgumentException.class, () -> new CoverageService(db).receive(event));
    }
}
