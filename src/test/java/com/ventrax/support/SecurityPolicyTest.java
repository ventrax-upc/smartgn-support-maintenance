package com.ventrax.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ventrax.support.infrastructure.security.AccessPolicy;

import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

class SecurityPolicyTest {
    AccessPolicy policy;
    UUID id = UUID.randomUUID();

    @BeforeEach
    void setup() {
        policy =
                new AccessPolicy(
                        mock(JdbcTemplate.class),
                        "http://127.0.0.1:1",
                        "http://127.0.0.1:1",
                        "test");
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    void actor(String role) {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                Map.of(
                                        "accountId",
                                        id.toString(),
                                        "role",
                                        role,
                                        "accountKind",
                                        "PRINCIPAL"),
                                null,
                                List.of()));
    }

    @Test
    void userCannotAdminister() {
        actor("USER");
        assertThrows(org.springframework.web.server.ResponseStatusException.class, policy::admin);
    }

    @Test
    void superadminMayAdminister() {
        actor("SUPERADMIN");
        assertDoesNotThrow(policy::admin);
    }

    @Test
    void forgedAuthorRejected() {
        actor("USER");
        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> policy.identity(UUID.randomUUID(), id));
    }

    @Test
    void actualAuthorAccepted() {
        actor("USER");
        assertDoesNotThrow(() -> policy.identity(id, id));
    }

    @Test
    void missingOwnershipServiceDoesNotGrant() {
        actor("USER");
        assertThrows(
                org.springframework.web.client.RestClientException.class,
                () -> policy.meter(UUID.randomUUID(), "USE_TICKETS"));
    }
}
