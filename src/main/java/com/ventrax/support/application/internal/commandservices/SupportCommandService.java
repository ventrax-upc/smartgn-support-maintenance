package com.ventrax.support.application.internal.commandservices;

import com.ventrax.support.application.outbound.AccessPort;
import com.ventrax.support.application.outbound.persistence.*;
import com.ventrax.support.domain.model.aggregates.SupportTicket;
import com.ventrax.support.domain.model.aggregates.TechnicalVisit;
import com.ventrax.support.domain.model.entities.*;
import com.ventrax.support.domain.model.valueobjects.VisitStatus;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class SupportCommandService {

    private final KnowledgeArticleRepositoryPort articleRepo;
    private final TechnicianRepositoryPort technicianRepo;
    private final SupportTicketRepositoryPort ticketRepo;
    private final TicketReplyRepositoryPort replyRepo;
    private final TicketPhotoRepositoryPort photoRepo;
    private final ServiceRatingRepositoryPort ratingRepo;
    private final TechnicalVisitRepositoryPort visitRepo;
    private final TechnicianAssignmentRepositoryPort assignmentRepo;
    private final VisitDiagnosisRepositoryPort diagnosisRepo;
    private final JdbcTemplate db;
    private final AccessPort access;

    public SupportCommandService(
            KnowledgeArticleRepositoryPort articleRepo,
            TechnicianRepositoryPort technicianRepo,
            SupportTicketRepositoryPort ticketRepo,
            TicketReplyRepositoryPort replyRepo,
            TicketPhotoRepositoryPort photoRepo,
            ServiceRatingRepositoryPort ratingRepo,
            TechnicalVisitRepositoryPort visitRepo,
            TechnicianAssignmentRepositoryPort assignmentRepo,
            VisitDiagnosisRepositoryPort diagnosisRepo,
            JdbcTemplate db,
            AccessPort access) {
        this.articleRepo = articleRepo;
        this.technicianRepo = technicianRepo;
        this.ticketRepo = ticketRepo;
        this.replyRepo = replyRepo;
        this.photoRepo = photoRepo;
        this.ratingRepo = ratingRepo;
        this.visitRepo = visitRepo;
        this.assignmentRepo = assignmentRepo;
        this.diagnosisRepo = diagnosisRepo;
        this.db = db;
        this.access = access;
    }

    private void audit(String action, UUID resource) {
        db.update(
                "insert into support_audit(id,actor_id,action,resource_id) values(?,?,?,?)",
                UUID.randomUUID(),
                access.actorId(),
                action,
                resource);
        if (!"TechnicalVisitCompleted".equals(action))
            db.update(
                    "insert into support_lifecycle_outbox(id,action,resource_id,actor_id)"
                            + " values(?,?,?,?)",
                    UUID.randomUUID(),
                    action,
                    resource,
                    access.actorId());
    }

    public KnowledgeArticle createArticle(String title, String category, String contentReference) {
        return articleRepo.save(new KnowledgeArticle(title, category, contentReference));
    }

    public Technician createTechnician(
            String fullName,
            String accreditationCode,
            String specialty,
            String email,
            String phone) {
        return technicianRepo.save(
                new Technician(fullName, accreditationCode, specialty, email, phone));
    }

    public Technician updateTechnician(
            UUID technicianId,
            String accreditationCode,
            String specialty,
            String email,
            String phone,
            Boolean available,
            Boolean active) {
        Technician tech =
                technicianRepo
                        .findById(technicianId)
                        .orElseThrow(() -> new IllegalArgumentException("Técnico no encontrado"));
        if ((Boolean.TRUE.equals(available) || Boolean.FALSE.equals(active))
                && !db.queryForList(
                                "select assignment_id from technician_assignments where"
                                        + " technician_id=? and active",
                                technicianId)
                        .isEmpty())
            throw new IllegalStateException("Technician has an active assignment");
        tech.updateDetails(accreditationCode, specialty, email, phone, available, active);
        return technicianRepo.save(tech);
    }

    public SupportTicket createTicket(
            UUID principalAccountId, UUID meterId, String subject, String description) {
        UUID accountId = access.meter(meterId, "USE_TICKETS");
        access.identity(principalAccountId, accountId);
        if (subject == null
                || subject.isBlank()
                || subject.length() > 180
                || description == null
                || description.isBlank())
            throw new IllegalArgumentException("Subject and description required");
        var result = ticketRepo.save(new SupportTicket(accountId, meterId, subject, description));
        audit("TicketCreated", result.getTicketId());
        return result;
    }

    public TicketReply addReply(UUID ticketId, UUID authorAccountId, String body) {
        db.queryForList(
                "select ticket_id from support_tickets where ticket_id=? for update", ticketId);
        ticketRepo
                .findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        UUID authorId = access.actorId();
        access.identity(authorAccountId, authorId);
        var ticket = ticketRepo.findById(ticketId).orElseThrow();
        ticket.writtenSupport();
        if (body == null || body.isBlank()) throw new IllegalArgumentException("Reply required");
        audit("TicketReplied", ticketId);
        return replyRepo.save(new TicketReply(ticketId, authorId, body));
    }

    public TicketPhoto addPhoto(UUID ticketId, String objectReference, UUID uploadedByAccountId) {
        db.queryForList(
                "select ticket_id from support_tickets where ticket_id=? for update", ticketId);
        ticketRepo
                .findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        UUID uploaderId = access.actorId();
        access.identity(uploadedByAccountId, uploaderId);
        ticketRepo.findById(ticketId).orElseThrow().writtenSupport();
        audit("TicketPhotoAdded", ticketId);
        return photoRepo.save(new TicketPhoto(ticketId, objectReference, uploaderId));
    }

    public TechnicalVisit requestVisit(UUID ticketId, Instant windowStart, Instant windowEnd) {
        db.queryForList(
                "select ticket_id from support_tickets where ticket_id=? for update", ticketId);
        SupportTicket ticket =
                ticketRepo
                        .findById(ticketId)
                        .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        if (windowStart == null
                || windowEnd == null
                || !windowStart.isBefore(windowEnd)
                || windowStart.isBefore(Instant.now()))
            throw new IllegalArgumentException("Invalid availability window");
        db.queryForList(
                "select ticket_id from support_tickets where ticket_id=? for update", ticketId);
        ticket.requestVisit();
        ticketRepo.save(ticket);
        var visit = visitRepo.saveAndFlush(new TechnicalVisit(ticketId, windowStart, windowEnd));
        var technicians =
                db.queryForList(
                        "select technician_id from technicians where active and available order by"
                                + " technician_id for update skip locked limit 1");
        if (!technicians.isEmpty())
            assignTechnician(
                    visit.getVisitId(), (UUID) technicians.getFirst().get("technician_id"));
        return visit;
    }

    public TechnicianAssignment assignTechnician(UUID visitId, UUID technicianId) {
        return assignTechnician(visitId, technicianId, "Automatic assignment");
    }

    public TechnicianAssignment assignTechnician(UUID visitId, UUID technicianId, String reason) {
        if (reason == null || reason.isBlank())
            throw new IllegalArgumentException("Assignment reason required");
        db.queryForList(
                "select visit_id from technical_visits where visit_id=? for update", visitId);
        db.queryForList(
                "select technician_id from technicians where technician_id=? for update",
                technicianId);
        TechnicalVisit visit =
                visitRepo
                        .findById(visitId)
                        .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada"));
        Technician tech =
                technicianRepo
                        .findById(technicianId)
                        .orElseThrow(() -> new IllegalArgumentException("Técnico no encontrado"));

        if (!Boolean.TRUE.equals(tech.getActive()) || !Boolean.TRUE.equals(tech.getAvailable()))
            throw new IllegalStateException("Technician unavailable");
        var old =
                db.queryForList(
                        "update technician_assignments set active=false where visit_id=? and active"
                                + " returning technician_id",
                        visitId);
        for (var row : old)
            db.update(
                    "update technicians set available=true where technician_id=?",
                    row.get("technician_id"));
        db.update(
                "insert into support_audit(id,actor_id,action,resource_id,detail)"
                        + " values(?,?,?,?,?)",
                UUID.randomUUID(),
                access.actorId(),
                "TECHNICIAN_ASSIGNED",
                visitId,
                "Technician " + technicianId + "; " + reason);
        visit.markAssigned();
        visitRepo.save(visit);
        tech.setAvailable(false);
        technicianRepo.save(tech);
        return assignmentRepo.save(new TechnicianAssignment(visitId, technicianId));
    }

    public VisitDiagnosis completeVisitWithDiagnosis(
            UUID visitId, String performedWork, String diagnosis, BigDecimal finalCost) {
        db.queryForList(
                "select visit_id from technical_visits where visit_id=? for update", visitId);
        TechnicalVisit visit =
                visitRepo
                        .findById(visitId)
                        .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada"));
        if (visit.getStatus() == VisitStatus.COMPLETED) {
            throw new IllegalStateException("La visita técnica ya fue completada previamente.");
        }
        visit.completeService();
        visitRepo.save(visit);

        SupportTicket ticket =
                ticketRepo
                        .findById(visit.getTicketId())
                        .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        ticket.markResolved();
        ticketRepo.save(ticket);

        if (finalCost == null || finalCost.signum() < 0 || finalCost.scale() > 2)
            throw new IllegalArgumentException("Invalid final cost");
        db.update(
                "update technicians set available=true where technician_id in (select technician_id"
                        + " from technician_assignments where visit_id=? and active)",
                visitId);
        db.update("update technician_assignments set active=false where visit_id=?", visitId);
        audit("TechnicalVisitCompleted", visitId);
        UUID billingRef = null;
        UUID event = UUID.randomUUID();
        String payload =
                "{\"eventId\":\""
                        + event
                        + "\",\"visitId\":\""
                        + visitId
                        + "\",\"accountId\":\""
                        + ticket.getPrincipalAccountId()
                        + "\",\"meterId\":\""
                        + ticket.getMeterId()
                        + "\",\"completedAt\":\""
                        + visit.getCompletedAt()
                        + "\",\"cost\":"
                        + finalCost
                        + "}";
        db.update(
                "insert into support_outbox(id,visit_id,payload) values(?,?,?)",
                event,
                visitId,
                payload);
        var result =
                diagnosisRepo.saveAndFlush(
                        new VisitDiagnosis(visitId, diagnosis, finalCost, billingRef));
        result.performedWork(performedWork);
        return result;
    }

    public void resolve(UUID id) {
        db.queryForList("select ticket_id from support_tickets where ticket_id=? for update", id);
        access.admin();
        var ticket = ticketRepo.findById(id).orElseThrow();
        if (!visitRepo.findByTicketId(id).stream()
                .allMatch(
                        v ->
                                v.getStatus() == VisitStatus.COMPLETED
                                        || v.getStatus() == VisitStatus.CANCELLED))
            throw new IllegalStateException("Active visit exists");
        ticket.markResolved();
        audit("TicketResolved", id);
    }

    public TechnicalVisit schedule(UUID id, Instant start, Instant end) {
        db.queryForList("select visit_id from technical_visits where visit_id=? for update", id);
        var visit = visitRepo.findById(id).orElseThrow();
        visit.schedule(start, end);
        audit("TechnicalVisitScheduled", id);
        return visit;
    }

    public TechnicalVisit start(UUID id) {
        db.queryForList("select visit_id from technical_visits where visit_id=? for update", id);
        var visit = visitRepo.findById(id).orElseThrow();
        visit.start();
        audit("TechnicalVisitStarted", id);
        return visit;
    }

    public SupportTicket closeTicket(UUID ticketId) {
        db.queryForList(
                "select ticket_id from support_tickets where ticket_id=? for update", ticketId);
        SupportTicket ticket =
                ticketRepo
                        .findById(ticketId)
                        .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        db.update(
                "insert into support_audit(id,actor_id,action,resource_id) values(?,?,?,?)",
                UUID.randomUUID(),
                access.actorId(),
                "TICKET_CLOSED",
                ticketId);
        ticket.close();
        return ticketRepo.save(ticket);
    }

    public ServiceRating rateTicket(UUID ticketId, Integer score, String comment) {
        db.queryForList(
                "select ticket_id from support_tickets where ticket_id=? for update", ticketId);
        SupportTicket ticket =
                ticketRepo
                        .findById(ticketId)
                        .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        if (score == null || score < 1 || score > 5) {
            throw new IllegalArgumentException("La puntuación debe estar entre 1 y 5.");
        }
        if (ticket.getStatus() != com.ventrax.support.domain.model.valueobjects.TicketStatus.CLOSED)
            throw new IllegalStateException("Close ticket before rating");
        audit("ServiceRated", ticketId);
        return ratingRepo.save(new ServiceRating(ticketId, score, comment));
    }
}
