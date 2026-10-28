package com.ventrax.smartgn.support.application.outbound;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class BillingCoverageClient {
    public UUID registerCompletedVisitCharge(UUID visitId, BigDecimal finalCost) {
        return UUID.randomUUID();
    }
}