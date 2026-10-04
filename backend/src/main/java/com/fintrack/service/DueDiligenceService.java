package com.fintrack.service;

import com.fintrack.entity.*;
import com.fintrack.exception.NotFoundException;
import com.fintrack.mock.SourceSimulator;
import com.fintrack.mock.SourceSimulator.Outcome;
import com.fintrack.repository.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ejecuta la debida diligencia simulada: consulta todas las fuentes ficticias y guarda su estado. */
@Service
public class DueDiligenceService {
    public static final String STATUS_COMPLETE = "VERIFICACIÓN COMPLETADA";
    public static final String STATUS_PARTIAL = "VERIFICACIÓN PARCIAL — FUENTES SIN RESPUESTA";

    private final PersonRepository persons;
    private final SourceRepository sources;
    private final SourceResultRepository results;
    private final DueDiligenceCheckRepository checks;
    private final AccountRepository accounts;
    private final CreditRepository credits;
    private final SanctionRepository sanctions;
    private final PepRecordRepository pepRecords;
    private final BackgroundCheckRepository backgroundChecks;
    private final JudicialProcessRepository processes;
    private final TrafficRecordRepository trafficRecords;
    private final ReputationalNewsRepository news;
    private final TimelineEventRepository timeline;
    private final SourceSimulator simulator;

    public DueDiligenceService(PersonRepository persons, SourceRepository sources, SourceResultRepository results,
                               DueDiligenceCheckRepository checks, AccountRepository accounts,
                               CreditRepository credits, SanctionRepository sanctions,
                               PepRecordRepository pepRecords, BackgroundCheckRepository backgroundChecks,
                               JudicialProcessRepository processes, TrafficRecordRepository trafficRecords,
                               ReputationalNewsRepository news, TimelineEventRepository timeline,
                               SourceSimulator simulator) {
        this.persons = persons;
        this.sources = sources;
        this.results = results;
        this.checks = checks;
        this.accounts = accounts;
        this.credits = credits;
        this.sanctions = sanctions;
        this.pepRecords = pepRecords;
        this.backgroundChecks = backgroundChecks;
        this.processes = processes;
        this.trafficRecords = trafficRecords;
        this.news = news;
        this.timeline = timeline;
        this.simulator = simulator;
    }

    /** Datos ficticios de la persona que las fuentes simuladas "conocen". */
    private record Context(Map<Long, List<MatchType>> records, Set<Long> bankIds) {
    }

    private Context context(Long personId) {
        Map<Long, List<MatchType>> records = new HashMap<>();
        sanctions.findByPersonIdOrderById(personId).forEach(r -> add(records, r.source, r.matchType));
        pepRecords.findByPersonIdOrderById(personId).forEach(r -> add(records, r.source, r.matchType));
        backgroundChecks.findByPersonIdOrderById(personId).forEach(r -> add(records, r.source, r.matchType));
        processes.findByPersonIdOrderById(personId).forEach(r -> add(records, r.source, r.matchType));
        trafficRecords.findByPersonIdOrderById(personId).forEach(r -> add(records, r.source, r.matchType));
        news.findByPersonIdOrderById(personId).forEach(r -> add(records, r.source, r.matchType));

        Set<Long> bankIds = new HashSet<>();
        accounts.findByPersonIdOrderById(personId).forEach(a -> bankIds.add(a.bank.id));
        credits.findByPersonIdOrderById(personId).forEach(c -> bankIds.add(c.bank.id));
        return new Context(records, bankIds);
    }

    private static void add(Map<Long, List<MatchType>> records, Source source, MatchType matchType) {
        records.computeIfAbsent(source.id, k -> new ArrayList<>()).add(matchType);
    }

    private Outcome simulate(Source source, Long personId, int run, Context ctx, boolean forceAvailable) {
        return simulator.query(source, personId, run, ctx.records().getOrDefault(source.id, List.of()),
                source.bankId != null && ctx.bankIds().contains(source.bankId), forceAvailable);
    }

    /** Resultado de una sola fuente simulada, sin guardar nada (lo usa la API mock). */
    @Transactional(readOnly = true)
    public Outcome preview(Person person, Source source, int run) {
        return simulate(source, person.id, run, context(person.id), false);
    }

    /**
     * Vuelve a ejecutar todas las fuentes simuladas para la persona.
     *
     * @param seed consulta inicial de la semilla (número de consulta 0)
     */
    @Transactional
    public DueDiligenceCheck run(Long personId, String triggeredBy, LocalDateTime when, boolean seed) {
        Person person = persons.findById(personId)
                .orElseThrow(() -> new NotFoundException("Persona ficticia no encontrada."));
        int run = seed ? 0 : person.runNumber + 1;
        // La persona de referencia arranca con todas las fuentes respondiendo; las siguientes consultas ya simulan fallos.
        boolean forceAvailable = seed && Person.BASELINE_DOCUMENT.equals(person.document);
        Context ctx = context(personId);

        DueDiligenceCheck check = new DueDiligenceCheck();
        check.personId = personId;
        check.runNumber = run;
        check.startedAt = when;
        check.status = "EN_CURSO";
        check.triggeredBy = triggeredBy;
        checks.save(check);

        Map<Long, SourceResult> existing = results.findByPersonIdOrderBySourceIdAsc(personId).stream()
                .collect(Collectors.toMap(r -> r.source.id, Function.identity()));
        int findings = 0;
        int unavailable = 0;
        List<Source> all = sources.findAllByOrderById();
        for (Source source : all) {
            Outcome outcome = simulate(source, personId, run, ctx, forceAvailable);
            SourceResult result = existing.getOrDefault(source.id, new SourceResult());
            result.checkId = check.id;
            result.personId = personId;
            result.source = source;
            result.status = outcome.status();
            result.matchType = outcome.matchType();
            result.matches = outcome.matches();
            result.summary = outcome.summary();
            result.checkedAt = when;
            results.save(result);
            if (outcome.status() == SourceStatus.HALLAZGO) {
                findings++;
            }
            if (outcome.status().isFailure()) {
                unavailable++;
            }
        }

        check.totalSources = all.size();
        check.withFindings = findings;
        check.unavailable = unavailable;
        check.finishedAt = when;
        check.status = unavailable == 0 ? "COMPLETADA" : "COMPLETADA_CON_FUENTES_SIN_RESPUESTA";
        checks.save(check);

        person.runNumber = run;
        person.lastCheckedAt = when;
        person.overallStatus = unavailable == 0 ? STATUS_COMPLETE : STATUS_PARTIAL;
        persons.save(person);

        if (!seed) {
            TimelineEvent event = new TimelineEvent();
            event.personId = personId;
            event.eventType = "CONSULTA";
            event.description = "Consulta simulada actualizada: " + all.size() + " fuentes, " + findings
                    + " con hallazgo, " + unavailable + " sin respuesta.";
            event.occurredAt = when;
            timeline.save(event);
        }
        return check;
    }
}
