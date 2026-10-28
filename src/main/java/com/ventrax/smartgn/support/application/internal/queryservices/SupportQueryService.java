package com.ventrax.smartgn.support.application.internal.queryservices;

import com.ventrax.smartgn.support.domain.model.aggregates.SupportTicket;
import com.ventrax.smartgn.support.domain.model.aggregates.TechnicalVisit;
import com.ventrax.smartgn.support.domain.model.entities.*;
import com.ventrax.smartgn.support.infrastructure.persistence.jpa.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SupportQueryService {

    private final KnowledgeArticleRepository articleRepo;
    private final TechnicianRepository technicianRepo;
    private final SupportTicketRepository ticketRepo;
    private final TicketReplyRepository replyRepo;
    private final TicketPhotoRepository photoRepo;
    private final TechnicalVisitRepository visitRepo;
    private final VisitDiagnosisRepository diagnosisRepo;
    private final ServiceRatingRepository ratingRepo;

    public SupportQueryService(
            KnowledgeArticleRepository articleRepo,
            TechnicianRepository technicianRepo,
            SupportTicketRepository ticketRepo,
            TicketReplyRepository replyRepo,
            TicketPhotoRepository photoRepo,
            TechnicalVisitRepository visitRepo,
            VisitDiagnosisRepository diagnosisRepo,
            ServiceRatingRepository ratingRepo) {
        this.articleRepo = articleRepo;
        this.technicianRepo = technicianRepo;
        this.ticketRepo = ticketRepo;
        this.replyRepo = replyRepo;
        this.photoRepo = photoRepo;
        this.visitRepo = visitRepo;
        this.diagnosisRepo = diagnosisRepo;
        this.ratingRepo = ratingRepo;
    }

    public List<KnowledgeArticle> getActiveArticles() { return articleRepo.findByActiveTrue(); }
    public List<Technician> getAvailableTechnicians() { return technicianRepo.findByAvailableTrueAndActiveTrue(); }
    public List<SupportTicket> getAllTickets() { return ticketRepo.findAll(); }
    public Optional<SupportTicket> getTicketById(UUID ticketId) { return ticketRepo.findById(ticketId); }
    public List<TicketReply> getRepliesByTicket(UUID ticketId) { return replyRepo.findByTicketIdOrderByPostedAtAsc(ticketId); }
    public List<TicketPhoto> getPhotosByTicket(UUID ticketId) { return photoRepo.findByTicketId(ticketId); }
    public List<TechnicalVisit> getVisitsByTicket(UUID ticketId) { return visitRepo.findByTicketId(ticketId); }
    public Optional<VisitDiagnosis> getDiagnosisByVisit(UUID visitId) { return diagnosisRepo.findByVisitId(visitId); }
    public Optional<ServiceRating> getRatingByTicket(UUID ticketId) { return ratingRepo.findByTicketId(ticketId); }
}