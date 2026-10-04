package com.fintrack.mock;

import com.fintrack.entity.Bank;
import com.fintrack.entity.Person;
import com.fintrack.entity.Source;
import com.fintrack.exception.NotFoundException;
import com.fintrack.repository.AccountRepository;
import com.fintrack.repository.BankRepository;
import com.fintrack.repository.CreditRepository;
import com.fintrack.repository.PersonRepository;
import com.fintrack.repository.SourceRepository;
import com.fintrack.service.DueDiligenceService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/**
 * APIs simuladas e independientes por banco y por fuente (/api/mock/banco-nova, etc.).
 * Responden únicamente con datos ficticios de la base de datos local; no existe ninguna llamada externa.
 */
@RestController
@RequestMapping("/api/mock")
public class MockApiController {
    private static final String NOTE = "API simulada — no conecta con ninguna entidad real";

    private final BankRepository banks;
    private final PersonRepository persons;
    private final AccountRepository accounts;
    private final CreditRepository credits;
    private final SourceRepository sources;
    private final DueDiligenceService dueDiligence;

    public MockApiController(BankRepository banks, PersonRepository persons, AccountRepository accounts,
                             CreditRepository credits, SourceRepository sources, DueDiligenceService dueDiligence) {
        this.banks = banks;
        this.persons = persons;
        this.accounts = accounts;
        this.credits = credits;
        this.sources = sources;
        this.dueDiligence = dueDiligence;
    }

    @GetMapping("/sources/{code}")
    public Map<String, Object> source(@PathVariable String code, @RequestParam String document,
                                      @RequestParam(defaultValue = "1") int run) {
        Source source = sources.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Fuente simulada no encontrada."));
        Person person = person(document);
        Map<String, Object> body = base(source.name);
        body.put("consulta", run);
        body.put("resultado", dueDiligence.preview(person, source, run));
        return body;
    }

    @GetMapping("/{slug}")
    public Map<String, Object> bank(@PathVariable String slug) {
        Bank bank = findBank(slug);
        Map<String, Object> body = base(bank.name);
        body.put("endpoints", new String[]{"/api/mock/" + slug + "/accounts?document=", "/api/mock/" + slug + "/credits?document="});
        return body;
    }

    @GetMapping("/{slug}/accounts")
    public Map<String, Object> accounts(@PathVariable String slug, @RequestParam String document) {
        Bank bank = findBank(slug);
        Map<String, Object> body = base(bank.name);
        body.put("cuentas", accounts.findByPersonIdAndBankId(person(document).id, bank.id));
        return body;
    }

    @GetMapping("/{slug}/credits")
    public Map<String, Object> credits(@PathVariable String slug, @RequestParam String document) {
        Bank bank = findBank(slug);
        Map<String, Object> body = base(bank.name);
        body.put("creditos", credits.findByPersonIdAndBankId(person(document).id, bank.id));
        return body;
    }

    private Bank findBank(String slug) {
        return banks.findBySlug(slug).orElseThrow(() -> new NotFoundException("Banco ficticio no encontrado."));
    }

    private Person person(String document) {
        return persons.findByDocument(document)
                .orElseThrow(() -> new NotFoundException("El documento no existe en la base de datos ficticia."));
    }

    private static Map<String, Object> base(String name) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fuente", name);
        body.put("aviso", NOTE);
        return body;
    }
}
