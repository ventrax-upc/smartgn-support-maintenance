package com.ventrax.smartgn.support.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class SupportResources {
    public record CreateArticleRequest(String title, String category, String contentReference) {}
    public record CreateTechnicianRequest(String fullName, String accreditationCode, String email, String phone) {}
    public record CreateTicketRequest(UUID principalAccountId, UUID meterId, String subject, String message) {}
    public record AddReplyRequest(UUID authorAccountId, String body) {}
    public record AddPhotoRequest(String objectReference, UUID uploadedByAccountId) {}
    public record RequestVisitRequest(Instant requestedWindowStart, Instant requestedWindowEnd) {}
    public record AssignTechnicianRequest(UUID technicianId) {}
    public record CompleteVisitDiagnosisRequest(String summary, BigDecimal finalCost) {}
    public record RateTicketRequest(Integer score, String comment) {}
}