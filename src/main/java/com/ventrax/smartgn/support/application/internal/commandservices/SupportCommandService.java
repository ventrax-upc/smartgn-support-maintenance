package com.ventrax.smartgn.support.application.internal.commandservices;

import com.ventrax.smartgn.support.application.outbound.BillingCoverageClient;
import com.ventrax.smartgn.support.domain.model.aggregates.SupportTicket;
import com.ventrax.smartgn.support.domain.model.aggregates.TechnicalVisit;
import com.ventrax.smartgn.support.domain.model.entities.*;
import com.ventrax.smartgn.support.infrastructure.persistence.jpa.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class SupportCommandService {

    private final KnowledgeArticleRepository articleRepo;
    private final TechnicianRepository technicianRepo;
    private final SupportTicketRepository ticketRepo;
    private final TicketReplyRepository replyRepo;
    private final TicketPhotoRepository photoRepo;
    private final ServiceRatingRepository ratingRepo;
    private final TechnicalVisitRepository visitRepo;
    private final TechnicianAssignmentRepository assignmentRepo;
    private final VisitDiagnosisRepository diagnosisRepo;
    private final BillingCoverageClient billingClient;

    public SupportCommandService(
            KnowledgeArticleRepository articleRepo,
            TechnicianRepository technicianRepo,
            SupportTicketRepository ticketRepo,
            TicketReplyRepository replyRepo,
            TicketPhotoRepository photoRepo,
            ServiceRatingRepository ratingRepo,
            TechnicalVisitRepository visitRepo,
            TechnicianAssignmentRepository assignmentRepo,
            VisitDiagnosisRepository diagnosisRepo,
            BillingCoverageClient billingClient) {
        this.articleRepo = articleRepo;
        this.technicianRepo = technicianRepo;
        this.ticketRepo = ticketRepo;
        this.replyRepo = replyRepo;
        this.photoRepo = photoRepo;
        this.ratingRepo = ratingRepo;
        this.visitRepo = visitRepo;
        this.assignmentRepo = assignmentRepo;
        this.diagnosisRepo = diagnosisRepo;
        this.billingClient = billingClient;
    }

    public KnowledgeArticle createArticle(String title, String category, String contentReference) {
        return articleRepo.save(new KnowledgeArticle(title, category, contentReference));
    }

    public Technician createTechnician(String fullName, String accreditationCode, String email, String phone) {
        return technicianRepo.save(new Technician(fullName, accreditationCode, email, phone));
    }

    public SupportTicket createTicket(UUID principalAccountId, UUID meterId, String subject, String message) {
        return ticketRepo.save(new SupportTicket(principalAccountId, meterId, subject, message));
    }

    public TicketReply addReply(UUID ticketId, UUID authorAccountId, String body) {
        ticketRepo.findById(ticketId).orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        return replyRepo.save(new TicketReply(ticketId, authorAccountId, body));
    }

    public TicketPhoto addPhoto(UUID ticketId, String objectReference, UUID uploadedByAccountId) {
        ticketRepo.findById(ticketId).orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        return photoRepo.save(new TicketPhoto(ticketId, objectReference, uploadedByAccountId));
    }

    public TechnicalVisit requestVisit(UUID ticketId, Instant windowStart, Instant windowEnd) {
        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        ticket.requestVisit();
        ticketRepo.save(ticket);
        return visitRepo.save(new TechnicalVisit(ticketId, windowStart, windowEnd));
    }

    public TechnicianAssignment assignTechnician(UUID visitId, UUID technicianId) {
        TechnicalVisit visit = visitRepo.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada"));
        Technician tech = technicianRepo.findById(technicianId)
                .orElseThrow(() -> new IllegalArgumentException("Técnico no encontrado"));
        visit.markAssigned();
        visitRepo.save(visit);
        tech.setAvailable(false);
        technicianRepo.save(tech);
        return assignmentRepo.save(new TechnicianAssignment(visitId, technicianId));
    }

    public VisitDiagnosis completeVisitWithDiagnosis(UUID visitId, String summary, BigDecimal finalCost) {
        TechnicalVisit visit = visitRepo.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada"));
        visit.completeService();
        visitRepo.save(visit);

        SupportTicket ticket = ticketRepo.findById(visit.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        ticket.markResolved();
        ticketRepo.save(ticket);

        UUID billingRef = billingClient.registerCompletedVisitCharge(visitId, finalCost);
        return diagnosisRepo.save(new VisitDiagnosis(visitId, summary, finalCost, billingRef));
    }

    public SupportTicket closeTicket(UUID ticketId) {
        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        ticket.close();
        return ticketRepo.save(ticket);
    }

    public ServiceRating rateTicket(UUID ticketId, Integer score, String comment) {
        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("La puntuación debe estar entre 1 y 5.");
        }
        ticket.close();
        ticketRepo.save(ticket);
        return ratingRepo.save(new ServiceRating(ticketId, score, comment));
    }
}