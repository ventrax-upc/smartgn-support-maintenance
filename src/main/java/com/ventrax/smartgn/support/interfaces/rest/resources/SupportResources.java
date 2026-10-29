package com.ventrax.smartgn.support.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class SupportResources {
    public record CreateArticleRequest(String title, String category, String contentReference) {}
    public record CreateTechnicianRequest(String fullName, String accreditationCode, String specialty, String email, String phone) {}
    public record UpdateTechnicianRequest(String accreditationCode, String specialty, String email, String phone, Boolean available, Boolean active) {}
    public record CreateTicketRequest(UUID principalAccountId, UUID meterId, String subject, String description) {}
    public record AddReplyRequest(UUID authorAccountId, String body) {}
    public record RequestVisitRequest(Instant requestedWindowStart, Instant requestedWindowEnd) {}
    public record AssignTechnicianRequest(UUID technicianId) {}
    public record CompleteVisitDiagnosisRequest(String diagnosis, BigDecimal finalCost) {}
    public record RateTicketRequest(Integer score, String comment) {}
}