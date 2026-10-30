package com.ventrax.support.infrastructure.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@EnableScheduling
public class BillingDelivery {
    private final JdbcTemplate db;
    private final RestClient billing;

    public BillingDelivery(
            JdbcTemplate db,
            @Value("${smartgn.billing-url}") String url,
            @Value("${smartgn.internal-token}") String token) {
        this.db = db;
        var f =
                new org.springframework.http.client.JdkClientHttpRequestFactory(
                        java.net.http.HttpClient.newBuilder()
                                .connectTimeout(java.time.Duration.ofSeconds(2))
                                .build());
        f.setReadTimeout(java.time.Duration.ofSeconds(3));
        billing =
                RestClient.builder()
                        .baseUrl(url)
                        .requestFactory(f)
                        .defaultHeader("Authorization", "Bearer " + token)
                        .build();
    }

    @Scheduled(fixedDelay = 5000)
    public void deliver() {
        for (var row :
                db.queryForList(
                        "update support_outbox set next_attempt=now()+interval '30"
                            + " seconds',attempts=attempts+1 where id in (select id from"
                            + " support_outbox where delivered_at is null and next_attempt<=now()"
                            + " order by created_at limit 10 for update skip locked) returning *"))
            try {
                billing.post()
                        .uri("/internal/v1/technical-visits/completions")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(row.get("payload"))
                        .retrieve()
                        .toBodilessEntity();
                db.update("update support_outbox set delivered_at=now() where id=?", row.get("id"));
            } catch (org.springframework.web.client.RestClientException retry) {
            }
    }
}
