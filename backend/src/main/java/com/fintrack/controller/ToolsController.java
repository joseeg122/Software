package com.fintrack.controller;

import com.fintrack.ai.LocalAiEngine;
import com.fintrack.audit.AuditService;
import com.fintrack.dto.AiRequest;
import com.fintrack.dto.AiResponse;
import com.fintrack.dto.SearchResult;
import com.fintrack.entity.Bank;
import com.fintrack.entity.Report;
import com.fintrack.entity.Source;
import com.fintrack.repository.BankRepository;
import com.fintrack.repository.SourceRepository;
import com.fintrack.service.InputGuard;
import com.fintrack.service.ProfileService;
import com.fintrack.service.ReportService;
import com.fintrack.service.SearchService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Reportes, buscador global, FinTrack AI y catálogos. */
@RestController
@RequestMapping("/api")
public class ToolsController {
    private final ReportService reports;
    private final SearchService search;
    private final LocalAiEngine ai;
    private final ProfileService profiles;
    private final BankRepository banks;
    private final SourceRepository sources;
    private final AuditService audit;

    public ToolsController(ReportService reports, SearchService search, LocalAiEngine ai, ProfileService profiles,
                           BankRepository banks, SourceRepository sources, AuditService audit) {
        this.reports = reports;
        this.search = search;
        this.ai = ai;
        this.profiles = profiles;
        this.banks = banks;
        this.sources = sources;
        this.audit = audit;
    }

    @PostMapping("/persons/{personId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public Report generateReport(@PathVariable Long personId) {
        return reports.generate(personId);
    }

    @GetMapping("/persons/{personId}/reports")
    public List<Report> reportsByPerson(@PathVariable Long personId) {
        return reports.byPerson(personId);
    }

    @GetMapping("/reports/{id}")
    public Report report(@PathVariable Long id) {
        return reports.get(id);
    }

    @GetMapping("/search")
    public List<SearchResult> search(@RequestParam("q") String query) {
        return search.search(query);
    }

    /** La IA solo recibe el perfil ficticio ya procesado por el backend; no consulta fuentes. */
    @PostMapping("/persons/{personId}/ai/ask")
    public AiResponse ask(@PathVariable Long personId, @Valid @RequestBody AiRequest request) {
        String question = InputGuard.check(request.question());
        audit.log("IA_PREGUNTA", "Persona " + personId + ": " + question);
        return ai.answer(question, profiles.snapshot(personId));
    }

    @GetMapping("/banks")
    public List<Bank> banks() {
        return banks.findAll(Sort.by("id"));
    }

    @GetMapping("/sources")
    public List<Source> sources() {
        return sources.findAllByOrderById();
    }
}
