package com.ventrax.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sun.net.httpserver.HttpServer;
import com.ventrax.support.infrastructure.integration.BillingDelivery;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.InetSocketAddress;
import java.util.*;

class DeliveryTest {
    @Test
    void outageRetainsReceiptAndRetryUsesSameEvidence() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var status = new java.util.concurrent.atomic.AtomicInteger(503);
        List<String> requests = new ArrayList<>();
        server.createContext(
                "/internal/v1/technical-visits/completions",
                exchange -> {
                    assertEquals(
                            "Bearer test", exchange.getRequestHeaders().getFirst("Authorization"));
                    requests.add(
                            new String(
                                    exchange.getRequestBody().readAllBytes(),
                                    java.nio.charset.StandardCharsets.UTF_8));
                    exchange.sendResponseHeaders(status.get(), -1);
                    exchange.close();
                });
        server.start();
        try {
            var db = mock(JdbcTemplate.class);
            var id = UUID.randomUUID();
            String payload = "{\"eventId\":\"" + id + "\"}";
            when(db.queryForList(anyString()))
                    .thenReturn(List.of(Map.of("id", id, "payload", payload)));
            var worker =
                    new BillingDelivery(
                            db, "http://127.0.0.1:" + server.getAddress().getPort(), "test");
            worker.deliver();
            verify(db, never()).update(contains("delivered_at=now"), eq(id));
            status.set(200);
            worker.deliver();
            verify(db).update(contains("delivered_at=now"), eq(id));
            assertEquals(List.of(payload, payload), requests);
        } finally {
            server.stop(0);
        }
    }
}
