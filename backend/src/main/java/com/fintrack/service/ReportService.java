package com.fintrack.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.audit.AuditService;
import com.fintrack.dto.ProfileSnapshot;
import com.fintrack.entity.Report;
import com.fintrack.exception.NotFoundException;
import com.fintrack.repository.ReportRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private static final DateTimeFormatter CODE_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ReportRepository reports;
    private final ProfileService profiles;
    private final ObjectMapper mapper;
    private final AuditService audit;

    public ReportService(ReportRepository reports, ProfileService profiles, ObjectMapper mapper,
                         AuditService audit) {
        this.reports = reports;
        this.profiles = profiles;
        this.mapper = mapper;
        this.audit = audit;
    }

    @Transactional
    public Report generate(Long personId) {
        ProfileSnapshot snapshot = profiles.snapshot(personId);
        LocalDateTime now = LocalDateTime.now();
        Report report = new Report();
        report.code = "RPT-SIM-" + now.format(CODE_TS) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        report.personId = personId;
        report.generatedAt = now;
        report.generatedBy = AuditService.currentUser();
        report.checkStatus = snapshot.person().overallStatus;
        try {
            report.snapshot = mapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el reporte.", e);
        }
        reports.save(report);
        audit.log("REPORTE_GENERADO", report.code + " para persona " + personId);
        return report;
    }

    @Transactional(readOnly = true)
    public Report get(Long id) {
        return reports.findById(id).orElseThrow(() -> new NotFoundException("Reporte no encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Report> byPerson(Long personId) {
        return reports.findByPersonIdOrderByIdDesc(personId);
    }
}
