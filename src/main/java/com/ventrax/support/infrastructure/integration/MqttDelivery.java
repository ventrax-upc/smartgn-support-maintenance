package com.ventrax.support.infrastructure.integration;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MqttDelivery implements AutoCloseable {
    @jakarta.annotation.PreDestroy
    public synchronized void close() {
        try {
            if (client != null) {
                if (client.isConnected()) client.disconnect();
                client.close();
            }
        } catch (MqttException failure) {
            org.slf4j.LoggerFactory.getLogger(MqttDelivery.class).warn("MQTT shutdown failed");
        }
    }

    private final JdbcTemplate db;
    private final String url, user, password;
    private MqttClient client;

    @org.springframework.beans.factory.annotation.Autowired
    private com.ventrax.support.application.internal.commandservices.CoverageService coverage;

    public MqttDelivery(
            JdbcTemplate db,
            @Value("${MQTT_URL:}") String url,
            @Value("${MQTT_USERNAME:support-service}") String user,
            @Value("${MQTT_PASSWORD:}") String password) {
        this.db = db;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Scheduled(fixedDelay = 5000)
    public synchronized void publish() {
        if (url.isBlank()) return;
        try {
            if (client == null)
                client =
                        new MqttClient(
                                url,
                                System.getenv().getOrDefault("MQTT_CLIENT_ID", "smartgn-support"),
                                new org.eclipse.paho.client.mqttv3.persist.MemoryPersistence());
            if (!client.isConnected()) {
                var options = new MqttConnectOptions();
                options.setUserName(user);
                options.setPassword(password.toCharArray());
                options.setConnectionTimeout(3);
                options.setCleanSession(false);
                client.setCallback(
                        new MqttCallback() {
                            public void connectionLost(Throwable cause) {}

                            public void deliveryComplete(IMqttDeliveryToken token) {}

                            public void messageArrived(String topic, MqttMessage message)
                                    throws Exception {
                                try {
                                    Map event =
                                            new tools.jackson.databind.ObjectMapper()
                                                    .readValue(message.getPayload(), Map.class);
                                    if (event == null)
                                        throw new IllegalArgumentException("Event object required");
                                    coverage.receive(event);
                                } catch (IllegalArgumentException
                                        | IllegalStateException
                                        | tools.jackson.core.JacksonException invalid) {
                                    org.slf4j.LoggerFactory.getLogger(MqttDelivery.class)
                                            .warn("Rejected invalid coverage event");
                                }
                            }
                        });
                client.connect(options);
                client.subscribe("smartgn/events/payments/CoverageCalculated", 1);
            }
            var json = new tools.jackson.databind.ObjectMapper();
            for (var row :
                    db.queryForList(
                            "select * from support_outbox where mqtt_delivered_at is null order by"
                                    + " created_at limit 10")) {
                var payload = json.readValue(row.get("payload").toString(), Map.class);
                var envelope = new LinkedHashMap<String, Object>();
                envelope.put("event_id", row.get("id"));
                envelope.put("event_type", "TechnicalVisitCompleted");
                envelope.put("aggregate_id", row.get("visit_id"));
                envelope.put("correlation_id", row.get("id"));
                envelope.put("payload", payload);
                client.publish(
                        "smartgn/events/support/TechnicalVisitCompleted",
                        json.writeValueAsBytes(envelope),
                        1,
                        false);
                db.update(
                        "update support_outbox set mqtt_delivered_at=now() where id=?",
                        row.get("id"));
            }
            for (var row :
                    db.queryForList(
                            "select * from support_lifecycle_outbox where"
                                    + " action<>'TechnicalVisitCompleted' and delivered_at is null"
                                    + " order by created_at limit 20")) {
                var payload = new LinkedHashMap<String, Object>();
                payload.put("resourceId", row.get("resource_id"));
                payload.put("occurredAt", row.get("created_at").toString());
                var envelope =
                        Map.of(
                                "event_id",
                                row.get("id"),
                                "event_type",
                                row.get("action"),
                                "aggregate_id",
                                row.get("resource_id"),
                                "correlation_id",
                                row.get("id"),
                                "payload",
                                payload);
                client.publish(
                        "smartgn/events/support/" + row.get("action"),
                        json.writeValueAsBytes(envelope),
                        1,
                        false);
                db.update(
                        "update support_lifecycle_outbox set delivered_at=now() where id=?",
                        row.get("id"));
            }
        } catch (Exception retry) {
        }
    }
}
