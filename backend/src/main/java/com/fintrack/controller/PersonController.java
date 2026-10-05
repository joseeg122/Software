package com.fintrack.controller;

import com.fintrack.audit.AuditService;
import com.fintrack.dto.DashboardDto;
import com.fintrack.dto.ProfileSnapshot;
import com.fintrack.entity.*;
import com.fintrack.repository.PersonRepository;
import com.fintrack.security.CurrentUser;
import com.fintrack.service.DueDiligenceService;
import com.fintrack.service.ProfileService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/** Perfil 360° de las personas ficticias. Cada sección sale del mismo perfil consolidado. */
@RestController
@RequestMapping("/api/persons")
public class PersonController {
    private final PersonRepository persons;
    private final ProfileService profiles;
    private final DueDiligenceService dueDiligence;
    private final AuditService audit;

    public PersonController(PersonRepository persons, ProfileService profiles, DueDiligenceService dueDiligence,
                            AuditService audit) {
        this.persons = persons;
        this.profiles = profiles;
        this.dueDiligence = dueDiligence;
        this.audit = audit;
    }

    @GetMapping
    public List<Person> list() {
        Long own = CurrentUser.restrictedPersonId();
        return own == null ? persons.findAll(Sort.by("id")) : persons.findAllById(List.of(own));
    }

    @GetMapping("/{id}")
    public Person get(@PathVariable Long id) {
        return profiles.person(id);
    }

    @GetMapping("/{id}/dashboard")
    public DashboardDto dashboard(@PathVariable Long id) {
        return profiles.dashboard(id);
    }

    @GetMapping("/{id}/profile")
    public ProfileSnapshot profile(@PathVariable Long id) {
        return profiles.snapshot(id);
    }

    /** "Actualizar consulta": vuelve a ejecutar todas las fuentes simuladas y registra la fecha. */
    @PostMapping("/{id}/refresh")
    public ProfileSnapshot refresh(@PathVariable Long id) {
        DueDiligenceCheck check = dueDiligence.run(id, AuditService.currentUser(), LocalDateTime.now(), false);
        audit.log("CONSULTA_ACTUALIZADA", "Persona " + id + ", consulta #" + check.runNumber + ", "
                + check.unavailable + " fuentes sin respuesta");
        return profiles.snapshot(id);
    }

    @GetMapping("/{id}/banks")
    public List<SourceResult> banks(@PathVariable Long id) {
        return byCategory(id, "BANCO");
    }

    @GetMapping("/{id}/accounts")
    public List<Account> accounts(@PathVariable Long id) {
        return profiles.snapshot(id).accounts();
    }

    @GetMapping("/{id}/transactions")
    public List<Transaction> transactions(@PathVariable Long id) {
        return profiles.snapshot(id).transactions();
    }

    @GetMapping("/{id}/credits")
    public List<Credit> credits(@PathVariable Long id) {
        return profiles.snapshot(id).credits();
    }

    @GetMapping("/{id}/credit-payments")
    public List<CreditPayment> creditPayments(@PathVariable Long id) {
        return profiles.snapshot(id).creditPayments();
    }

    @GetMapping("/{id}/credit-history")
    public List<CreditHistory> creditHistory(@PathVariable Long id) {
        return profiles.snapshot(id).creditHistory();
    }

    @GetMapping("/{id}/score")
    public CreditScore score(@PathVariable Long id) {
        return profiles.snapshot(id).score();
    }

    @GetMapping("/{id}/due-diligence")
    public List<SourceResult> dueDiligence(@PathVariable Long id, @RequestParam(required = false) String category) {
        return category == null ? profiles.snapshot(id).sourceResults() : byCategory(id, category);
    }

    @GetMapping("/{id}/sources/unavailable")
    public List<SourceResult> unavailable(@PathVariable Long id) {
        return profiles.snapshot(id).unavailableSources();
    }

    @GetMapping("/{id}/sanctions")
    public List<Sanction> sanctions(@PathVariable Long id) {
        return profiles.snapshot(id).sanctions();
    }

    @GetMapping("/{id}/pep")
    public List<PepRecord> pep(@PathVariable Long id) {
        return profiles.snapshot(id).pepRecords();
    }

    @GetMapping("/{id}/background")
    public List<BackgroundCheck> background(@PathVariable Long id) {
        return profiles.snapshot(id).backgroundChecks();
    }

    @GetMapping("/{id}/judicial/processes")
    public List<JudicialProcess> processes(@PathVariable Long id) {
        return profiles.snapshot(id).judicialProcesses();
    }

    @GetMapping("/{id}/judicial/lawsuits")
    public List<Lawsuit> lawsuits(@PathVariable Long id) {
        return profiles.snapshot(id).lawsuits();
    }

    @GetMapping("/{id}/judicial/measures")
    public List<LegalMeasure> measures(@PathVariable Long id) {
        return profiles.snapshot(id).legalMeasures();
    }

    @GetMapping("/{id}/traffic")
    public List<TrafficRecord> traffic(@PathVariable Long id) {
        return profiles.snapshot(id).trafficRecords();
    }

    @GetMapping("/{id}/news")
    public List<ReputationalNews> news(@PathVariable Long id) {
        return profiles.snapshot(id).news();
    }

    @GetMapping("/{id}/alerts")
    public List<Alert> alerts(@PathVariable Long id) {
        return profiles.snapshot(id).alerts();
    }

    @GetMapping("/{id}/timeline")
    public List<TimelineEvent> timeline(@PathVariable Long id) {
        return profiles.snapshot(id).timeline();
    }

    private List<SourceResult> byCategory(Long id, String category) {
        return profiles.snapshot(id).sourceResults().stream()
                .filter(r -> r.source.category.equalsIgnoreCase(category)).toList();
    }
}
