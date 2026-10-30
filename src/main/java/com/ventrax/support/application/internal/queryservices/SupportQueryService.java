package com.ventrax.support.application.internal.queryservices;

import com.ventrax.support.application.outbound.persistence.*;
import com.ventrax.support.domain.model.aggregates.SupportTicket;
import com.ventrax.support.domain.model.aggregates.TechnicalVisit;
import com.ventrax.support.domain.model.entities.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SupportQueryService {

    @org.springframework.beans.factory.annotation.Autowired
    private TechnicianAssignmentRepositoryPort assignments;

    private final KnowledgeArticleRepositoryPort articleRepo;
    private final TechnicianRepositoryPort technicianRepo;
    private final SupportTicketRepositoryPort ticketRepo;
    private final TicketReplyRepositoryPort replyRepo;
    private final TicketPhotoRepositoryPort photoRepo;
    private final TechnicalVisitRepositoryPort visitRepo;
    private final VisitDiagnosisRepositoryPort diagnosisRepo;
    private final ServiceRatingRepositoryPort ratingRepo;

    public SupportQueryService(
            KnowledgeArticleRepositoryPort articleRepo,
            TechnicianRepositoryPort technicianRepo,
            SupportTicketRepositoryPort ticketRepo,
            TicketReplyRepositoryPort replyRepo,
            TicketPhotoRepositoryPort photoRepo,
            TechnicalVisitRepositoryPort visitRepo,
            VisitDiagnosisRepositoryPort diagnosisRepo,
            ServiceRatingRepositoryPort ratingRepo) {
        this.articleRepo = articleRepo;
        this.technicianRepo = technicianRepo;
        this.ticketRepo = ticketRepo;
        this.replyRepo = replyRepo;
        this.photoRepo = photoRepo;
        this.visitRepo = visitRepo;
        this.diagnosisRepo = diagnosisRepo;
        this.ratingRepo = ratingRepo;
    }

    public List<KnowledgeArticle> getActiveArticles() {
        return articleRepo.findByActiveTrue();
    }

    public List<Technician> getAvailableTechnicians() {
        return technicianRepo.findByAvailableTrueAndActiveTrue();
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.ventrax.support.application.outbound.AccessPort access;

    public org.springframework.data.domain.Page<SupportTicket> tickets(
            String status, int page, int size) {
        if (page < 0 || size < 1 || size > 100)
            throw new IllegalArgumentException("Invalid pagination");
        boolean admin = "SUPERADMIN".equals(access.actor().get("role"));
        var meters =
                admin ? java.util.Set.of(new UUID(0, 0)) : access.permittedMeters("USE_TICKETS");
        return ticketRepo.accessible(
                admin,
                java.util.UUID.fromString(
                        java.util.Objects.toString(
                                access.actor().get("principalAccountId"),
                                access.actor().get("accountId").toString())),
                meters.isEmpty() ? java.util.Set.of(new UUID(0, 0)) : meters,
                status == null
                        ? null
                        : com.ventrax.support.domain.model.valueobjects.TicketStatus.valueOf(
                                status),
                org.springframework.data.domain.PageRequest.of(
                        page,
                        size,
                        org.springframework.data.domain.Sort.by("createdAt", "ticketId")
                                .descending()));
    }

    public Optional<Technician> technician(UUID id) {
        return technicianRepo.findById(id);
    }

    public List<Technician> technicians() {
        return technicianRepo.findAll();
    }

    public Optional<SupportTicket> getTicketById(UUID ticketId) {
        return ticketRepo.findById(ticketId);
    }

    public List<TicketReply> getRepliesByTicket(UUID ticketId) {
        return replyRepo.findByTicketIdOrderByPostedAtAsc(ticketId);
    }

    public List<TicketPhoto> getPhotosByTicket(UUID ticketId) {
        return photoRepo.findByTicketId(ticketId);
    }

    public List<TechnicalVisit> getVisitsByTicket(UUID ticketId) {
        var result = visitRepo.findByTicketId(ticketId);
        result.forEach(
                v -> v.assignments(assignments.findByVisitIdOrderByAssignedAtAsc(v.getVisitId())));
        return result;
    }

    public Optional<VisitDiagnosis> getDiagnosisByVisit(UUID visitId) {
        return diagnosisRepo.findByVisitId(visitId);
    }

    public Optional<ServiceRating> getRatingByTicket(UUID ticketId) {
        return ratingRepo.findByTicketId(ticketId);
    }
}
