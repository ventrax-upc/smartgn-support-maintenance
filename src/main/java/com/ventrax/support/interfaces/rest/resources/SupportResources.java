package com.ventrax.support.interfaces.rest.resources;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class SupportResources {
    public record CreateArticleRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 80) String category,
            @NotBlank @Size(max = 255) String contentReference) {}

    public record CreateTechnicianRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Size(max = 60) String accreditationCode,
            @Size(max = 80) String specialty,
            @NotBlank @Email String email,
            @NotBlank @Size(max = 30) String phone) {}

    public record UpdateTechnicianRequest(
            String accreditationCode,
            String specialty,
            String email,
            String phone,
            Boolean available,
            Boolean active) {}

    public record CreateTicketRequest(
            UUID principalAccountId,
            @NotNull UUID meterId,
            @NotBlank @Size(max = 180) String subject,
            @NotBlank String description) {}

    public record AddReplyRequest(UUID authorAccountId, @NotBlank String body, Boolean resolve) {}

    public record RequestVisitRequest(Instant requestedWindowStart, Instant requestedWindowEnd) {}

    public record AssignTechnicianRequest(@NotNull UUID technicianId, @NotBlank String reason) {}

    public record CompleteVisitDiagnosisRequest(
            @NotBlank String performedWork,
            @NotBlank String diagnosis,
            @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal finalCost) {}

    public record RateTicketRequest(@NotNull @Min(1) @Max(5) Integer score, String comment) {}
}
