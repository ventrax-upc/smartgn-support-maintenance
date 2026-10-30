package com.ventrax.support;

import static org.junit.jupiter.api.Assertions.*;

import com.ventrax.support.domain.model.aggregates.*;
import com.ventrax.support.domain.model.valueobjects.*;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

class WorkflowTest {
    SupportTicket ticket() {
        return new SupportTicket(UUID.randomUUID(), UUID.randomUUID(), "Problem", "Description");
    }

    @Test
    void writtenAttention() {
        var t = ticket();
        t.writtenSupport();
        assertEquals(TicketStatus.WRITTEN_SUPPORT, t.getStatus());
        t.markResolved();
        t.close();
        var at = t.getClosedAt();
        assertThrows(IllegalStateException.class, t::close);
        assertEquals(at, t.getClosedAt());
        assertThrows(IllegalStateException.class, t::writtenSupport);
    }

    @Test
    void prematureClosureRejected() {
        assertThrows(IllegalStateException.class, () -> ticket().close());
    }

    @Test
    void visitLifecycle() {
        var now = Instant.now();
        var v = new TechnicalVisit(UUID.randomUUID(), now.plusSeconds(60), now.plusSeconds(3600));
        assertThrows(IllegalStateException.class, v::completeService);
        v.markAssigned();
        v.schedule(now.plusSeconds(120), now.plusSeconds(1800));
        v.start();
        v.completeService();
        assertEquals(VisitStatus.COMPLETED, v.getStatus());
        assertNotNull(v.getCompletedAt());
        assertThrows(IllegalStateException.class, v::completeService);
    }

    @Test
    void schedulingRequiresAssignment() {
        var v =
                new TechnicalVisit(
                        UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(100));
        assertThrows(IllegalStateException.class, () -> v.schedule(Instant.now(), Instant.now()));
    }

    @Test
    void invalidWindowRejected() {
        var now = Instant.now();
        var v = new TechnicalVisit(UUID.randomUUID(), now.plusSeconds(100), now.plusSeconds(1000));
        v.markAssigned();
        assertThrows(
                IllegalArgumentException.class,
                () -> v.schedule(now.plusSeconds(50), now.plusSeconds(900)));
    }
}
