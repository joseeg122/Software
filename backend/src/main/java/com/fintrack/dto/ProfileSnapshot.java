package com.fintrack.dto;

import com.fintrack.entity.*;
import java.util.List;

/** Perfil 360° completo de una persona ficticia. Es lo único que leen el reporte y FinTrack AI. */
public record ProfileSnapshot(Person person, DashboardDto dashboard, List<SourceResult> sourceResults,
                              List<SourceResult> unavailableSources, List<Account> accounts,
                              List<Transaction> transactions, List<Credit> credits,
                              List<CreditPayment> creditPayments, List<CreditHistory> creditHistory,
                              CreditScore score, List<Sanction> sanctions, List<PepRecord> pepRecords,
                              List<BackgroundCheck> backgroundChecks, List<JudicialProcess> judicialProcesses,
                              List<Lawsuit> lawsuits, List<LegalMeasure> legalMeasures,
                              List<TrafficRecord> trafficRecords, List<ReputationalNews> news, List<Alert> alerts,
                              List<Comment> comments, List<TimelineEvent> timeline, String disclaimer) {
    public static final String DISCLAIMER = "PROTOTIPO EDUCATIVO — DATOS COMPLETAMENTE SIMULADOS";
}
