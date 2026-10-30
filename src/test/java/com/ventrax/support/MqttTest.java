package com.ventrax.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ventrax.support.infrastructure.integration.MqttDelivery;

import org.eclipse.paho.client.mqttv3.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

@EnabledIfEnvironmentVariable(named = "TEST_MQTT_URL", matches = ".+")
class MqttTest {
    @Test
    void authenticatedBrokerReceivesStableCompletion() throws Exception {
        var client =
                new MqttClient(
                        System.getenv("TEST_MQTT_URL"),
                        "verification-" + UUID.randomUUID(),
                        new org.eclipse.paho.client.mqttv3.persist.MemoryPersistence());
        var options = new MqttConnectOptions();
        options.setUserName("support-service");
        options.setPassword(System.getenv("TEST_MQTT_PASSWORD").toCharArray());
        client.connect(options);
        var received = new java.util.concurrent.ArrayBlockingQueue<String>(1);
        client.setCallback(
                new MqttCallback() {
                    public void connectionLost(Throwable t) {}

                    public void deliveryComplete(IMqttDeliveryToken t) {}

                    public void messageArrived(String topic, MqttMessage m) {
                        received.offer(
                                new String(
                                        m.getPayload(), java.nio.charset.StandardCharsets.UTF_8));
                    }
                });
        client.subscribe("smartgn/events/support/TechnicalVisitCompleted", 1);
        try {
            var db = mock(JdbcTemplate.class);
            var event = UUID.randomUUID();
            var visit = UUID.randomUUID();
            when(db.queryForList(contains("from support_outbox")))
                    .thenReturn(
                            List.of(
                                    Map.of(
                                            "id",
                                            event,
                                            "visit_id",
                                            visit,
                                            "payload",
                                            "{\"visitId\":\"" + visit + "\"}")));
            when(db.queryForList(contains("support_lifecycle_outbox"))).thenReturn(List.of());
            var worker =
                    new MqttDelivery(
                            db,
                            System.getenv("TEST_MQTT_URL"),
                            "support-service",
                            System.getenv("TEST_MQTT_PASSWORD"));
            worker.publish();
            worker.close();
            String raw = received.poll(5, java.util.concurrent.TimeUnit.SECONDS);
            assertNotNull(raw);
            Map result = new tools.jackson.databind.ObjectMapper().readValue(raw, Map.class);
            assertEquals(event.toString(), result.get("event_id"));
            assertEquals("TechnicalVisitCompleted", result.get("event_type"));
            verify(db).update(contains("mqtt_delivered_at=now"), eq(event));
        } finally {
            client.disconnect();
            client.close();
        }
    }

    @Test
    void brokerRejectsInvalidCredential() throws Exception {
        var client =
                new MqttClient(
                        System.getenv("TEST_MQTT_URL"),
                        "invalid-" + UUID.randomUUID(),
                        new org.eclipse.paho.client.mqttv3.persist.MemoryPersistence());
        var options = new MqttConnectOptions();
        options.setUserName("support-service");
        options.setPassword("wrong".toCharArray());
        try {
            assertThrows(MqttException.class, () -> client.connect(options));
        } finally {
            client.close();
        }
    }
}
