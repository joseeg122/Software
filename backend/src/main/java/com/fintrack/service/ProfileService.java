package com.fintrack.service;

import com.fintrack.dto.DashboardDto;
import com.fintrack.dto.ProfileSnapshot;
import com.fintrack.entity.*;
import com.fintrack.exception.NotFoundException;
import com.fintrack.repository.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileService {
    public static final String CLOSED = "CERRADO";

    private final PersonRepository persons;
    private final SourceResultRepository results;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final CreditRepository credits;
    private final CreditPaymentRepository payments;
    private final CreditHistoryRepository history;
    private final CreditScoreRepository scores;
    private final SanctionRepository sanctions;
    private final PepRecordRepository pepRecords;
    private final BackgroundCheckRepository backgroundChecks;
    private final JudicialProcessRepository processes;
    private final LawsuitRepository lawsuits;
    private final LegalMeasureRepository measures;
    private final TrafficRecordRepository trafficRecords;
    private final ReputationalNewsRepository news;
    private final AlertRepository alerts;
    private final CommentRepository comments;
    private final TimelineEventRepository timeline;

    public ProfileService(PersonRepository persons, SourceResultRepository results, AccountRepository accounts,
                          TransactionRepository transactions, CreditRepository credits,
                          CreditPaymentRepository payments, CreditHistoryRepository history,
                          CreditScoreRepository scores, SanctionRepository sanctions,
                          PepRecordRepository pepRecords, BackgroundCheckRepository backgroundChecks,
                          JudicialProcessRepository processes, LawsuitRepository lawsuits,
                          LegalMeasureRepository measures, TrafficRecordRepository trafficRecords,
                          ReputationalNewsRepository news, AlertRepository alerts, CommentRepository comments,
                          TimelineEventRepository timeline) {
        this.persons = persons;
        this.results = results;
        this.accounts = accounts;
        this.transactions = transactions;
        this.credits = credits;
        this.payments = payments;
        this.history = history;
        this.scores = scores;
        this.sanctions = sanctions;
        this.pepRecords = pepRecords;
        this.backgroundChecks = backgroundChecks;
        this.processes = processes;
        this.lawsuits = lawsuits;
        this.measures = measures;
        this.trafficRecords = trafficRecords;
        this.news = news;
        this.alerts = alerts;
        this.comments = comments;
        this.timeline = timeline;
    }

    public Person person(Long id) {
        return persons.findById(id).orElseThrow(() -> new NotFoundException("Persona ficticia no encontrada."));
    }

    public DashboardDto dashboard(Long personId) {
        return snapshot(personId).dashboard();
    }

    public ProfileSnapshot snapshot(Long personId) {
        Person person = person(personId);
        List<SourceResult> sourceResults = results.findByPersonIdOrderBySourceIdAsc(personId);
        List<SourceResult> unavailable = sourceResults.stream().filter(r -> r.status.isFailure()).toList();
        List<Account> accountList = accounts.findByPersonIdOrderById(personId);
        List<Credit> creditList = credits.findByPersonIdOrderById(personId);
        List<JudicialProcess> processList = processes.findByPersonIdOrderById(personId);
        List<Alert> alertList = alerts.findByPersonIdOrderByCreatedAtDesc(personId);
        CreditScore score = scores.findFirstByPersonIdOrderByCalculatedAtDesc(personId).orElse(null);

        List<Credit> active = creditList.stream().filter(c -> !CLOSED.equals(c.status)).toList();
        BigDecimal debt = active.stream().map(c -> c.balance).reduce(BigDecimal.ZERO, BigDecimal::add);
        long banksConnected = sourceResults.stream()
                .filter(r -> "BANCO".equals(r.source.category) && r.status == SourceStatus.DISPONIBLE).count();
        long findings = alertList.stream().filter(a -> a.severity != Severity.SIN_HALLAZGOS).count();

        DashboardDto dashboard = new DashboardDto(person.id, person.fullName, person.document, person.overallStatus,
                person.lastCheckedAt, banksConnected, accountList.size(), debt, active.size(), processList.size(),
                findings, score == null ? null : score.score, score == null ? null : score.maxScore,
                unavailable.size());

        return new ProfileSnapshot(person, dashboard, sourceResults, unavailable, accountList,
                transactions.findByPersonIdOrderByTxDateDesc(personId), creditList,
                payments.findByPersonIdOrderByDueDateDescIdAsc(personId),
                history.findByPersonIdOrderByEventDateDesc(personId), score,
                sanctions.findByPersonIdOrderById(personId), pepRecords.findByPersonIdOrderById(personId),
                backgroundChecks.findByPersonIdOrderById(personId), processList,
                lawsuits.findByPersonIdOrderById(personId), measures.findByPersonIdOrderById(personId),
                trafficRecords.findByPersonIdOrderById(personId), news.findByPersonIdOrderById(personId), alertList,
                comments.findByPersonIdOrderByCreatedAtAsc(personId),
                timeline.findTop10ByPersonIdOrderByOccurredAtDesc(personId), ProfileSnapshot.DISCLAIMER);
    }
}
