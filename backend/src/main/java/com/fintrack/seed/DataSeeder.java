package com.fintrack.seed;

import static com.fintrack.seed.TransactionGenerator.thousands;

import com.fintrack.entity.*;
import com.fintrack.repository.AppUserRepository;
import com.fintrack.service.DueDiligenceService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Datos semilla deterministas (Random con semilla fija y fecha base fija). TODO es ficticio:
 * 10 personas, 7 bancos, 30 cuentas, 200 movimientos, 20 créditos, 200 pagos, 15 procesos,
 * 10 demandas, 5 medidas cautelares y 30 alertas.
 */
@Component
public class DataSeeder implements CommandLineRunner {
    public static final LocalDate BASE = LocalDate.of(2026, 10, 3);
    public static final LocalDateTime BASE_TS = BASE.atTime(21, 0);

    private static final String[][] PERSONS = {
            {"Juan Pérez", "Bogotá", "Ingeniero de sistemas", "1988-04-12"},
            {"María Gómez", "Medellín", "Contadora", "1990-09-03"},
            {"Carlos Rodríguez", "Cali", "Funcionario público", "1979-01-25"},
            {"Ana Martínez", "Barranquilla", "Comerciante", "1985-06-17"},
            {"Luis Hernández", "Cartagena", "Contratista", "1975-11-30"},
            {"Laura Torres", "Bucaramanga", "Abogada", "1992-02-08"},
            {"Andrés Ramírez", "Pereira", "Empresario", "1981-07-21"},
            {"Sofía Castro", "Manizales", "Diseñadora", "1994-12-05"},
            {"Diego Morales", "Ibagué", "Transportador", "1983-03-14"},
            {"Valentina Rojas", "Santa Marta", "Médica", "1989-10-27"}};
    private static final String[] BANKS = {"Banco Nova", "Banco Andino", "Banco Capital", "Banco Horizonte",
            "Banco Centralia", "Banco Unión", "Banco Continental"};
    private static final String[] BANK_SLUGS = {"banco-nova", "banco-andino", "banco-capital", "banco-horizonte",
            "banco-centralia", "banco-union", "banco-continental"};
    private static final String[] CITIES = {"Bogotá", "Medellín", "Cali", "Barranquilla", "Cartagena", "Bucaramanga",
            "Pereira", "Manizales", "Ibagué", "Santa Marta", "Tunja", "Popayán", "Armenia", "Montería",
            "Villavicencio", "Pasto", "Neiva", "Valledupar", "Quibdó"};
    private static final String[][] CREDIT_TYPES = {{"Crédito libre inversión", "LIBRE_INVERSION"},
            {"Tarjeta de crédito", "TARJETA_CREDITO"}, {"Crédito de vehículo", "VEHICULO"},
            {"Crédito hipotecario", "HIPOTECARIO"}, {"Crédito rotativo", "ROTATIVO"},
            {"Microcrédito", "MICROCREDITO"}};
    private static final String[] CREDIT_STATUSES = {"AL_DIA", "EN_MORA", "AL_DIA", "CERRADO", "AL_DIA",
            "REESTRUCTURADO", "AL_DIA", "AL_DIA", "COBRANZA_JURIDICA", "AL_DIA", "CERRADO", "EN_MORA", "AL_DIA",
            "AL_DIA", "REESTRUCTURADO", "AL_DIA"};
    private static final String[] PLAINTIFFS = {"Cobranzas Simuladas Ltda.", "Inversiones Ficticias S.A.S.",
            "Conjunto Residencial Imaginario", "Comercializadora Inventada S.A."};
    private static final String[] PROCESS_TYPES = {"Ejecutivo singular", "Verbal sumario", "Ordinario laboral",
            "Restitución de inmueble"};
    private static final String[] PROCESS_STATUSES = {"ACTIVO", "TERMINADO", "ACTIVO", "ARCHIVADO"};

    @PersistenceContext
    private EntityManager em;
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final DueDiligenceService dueDiligence;
    private final String demoUsername;
    private final String demoPassword;

    private final Map<String, Source> sources = new LinkedHashMap<>();
    private final List<Bank> banks = new ArrayList<>();
    private final List<Person> persons = new ArrayList<>();
    private int sequence;

    public DataSeeder(AppUserRepository users, PasswordEncoder encoder, DueDiligenceService dueDiligence,
                      @Value("${app.demo.username}") String demoUsername,
                      @Value("${app.demo.password}") String demoPassword) {
        this.users = users;
        this.encoder = encoder;
        this.dueDiligence = dueDiligence;
        this.demoUsername = demoUsername;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDemoUser();
        if (em.createQuery("select count(p) from Person p", Long.class).getSingleResult() > 0) {
            seedClientUser();
            return;
        }
        Random rnd = new Random(42);
        seedBanksAndSources();
        seedPersons();
        seedAccounts(rnd);
        seedCredits(rnd);
        seedScores(rnd);
        seedJudicial(rnd);
        seedListsAndRecords();
        em.flush();
        for (Person person : persons) {
            seedAlerts(person);
            seedTimeline(person);
        }
        Comment comment = new Comment();
        comment.personId = persons.get(0).id;
        comment.author = demoUsername;
        comment.body = "Se recomienda revisar el proceso simulado antes de continuar.";
        comment.createdAt = BASE_TS.minusHours(3);
        comment.updatedAt = comment.createdAt;
        em.persist(comment);
        em.flush();
        for (Person person : persons) {
            dueDiligence.run(person.id, "semilla", BASE_TS, true);
        }
        seedClientUser();
    }

    /**
     * Usuario restringido a UNA persona (la primera): solo ve la información de esa persona.
     * Usa la misma contraseña de demostración; el analista conserva acceso a todas.
     */
    private void seedClientUser() {
        Long personId = em.createQuery("select min(p.id) from Person p", Long.class).getSingleResult();
        if (personId == null) {
            return;
        }
        AppUser client = users.findByUsername("cliente").orElseGet(AppUser::new);
        if (client.id != null && encoder.matches(demoPassword, client.passwordHash)) {
            return;
        }
        client.username = "cliente";
        client.passwordHash = encoder.encode(demoPassword);
        client.fullName = "Cliente de demostración";
        client.role = "CLIENTE";
        client.personId = personId;
        if (client.createdAt == null) {
            client.createdAt = LocalDateTime.now();
        }
        users.save(client);
    }

    /** El usuario de demostración toma su contraseña de APP_DEMO_PASSWORD; nunca del código. */
    private void seedDemoUser() {
        if (demoPassword == null || demoPassword.isBlank()) {
            throw new IllegalStateException("Define APP_DEMO_PASSWORD por variable de entorno.");
        }
        AppUser user = users.findByUsername(demoUsername).orElseGet(AppUser::new);
        if (user.id != null && encoder.matches(demoPassword, user.passwordHash)) {
            return;
        }
        user.username = demoUsername;
        user.passwordHash = encoder.encode(demoPassword);
        user.fullName = "Analista de demostración";
        user.role = "ANALISTA";
        if (user.createdAt == null) {
            user.createdAt = LocalDateTime.now();
        }
        users.save(user);
    }

    private void seedBanksAndSources() {
        for (int i = 0; i < BANKS.length; i++) {
            Bank bank = new Bank();
            bank.slug = BANK_SLUGS[i];
            bank.name = BANKS[i];
            em.persist(bank);
            banks.add(bank);
            source(bank.slug, bank.name, "BANCO", SourceStatus.NO_REGISTRA, null, bank.id);
        }
        String[][] binding = {{"VIN_US_FTO", "Organizaciones terroristas — Depto. de Estado EE.UU."},
                {"VIN_EU_CONS", "EU Consolidated Sanctions"}, {"VIN_EU_TERR", "EU Terrorism Sanctions"},
                {"VIN_UN_SC", "UN Security Council"}, {"VIN_OTRAS", "Otras listas internacionales"}};
        String[][] restrictive = {{"RES_PANAMA", "Panama Papers"}, {"RES_AECA", "AECA"}, {"RES_WB", "World Bank"},
                {"RES_BID", "Comité de Sanciones BID"}, {"RES_CANADA", "Canadian Autonomous Sanctions"},
                {"RES_BIS_DPL", "BIS Denied Persons"}, {"RES_BIS_EL", "BIS Entity List"}, {"RES_FCPA", "FCPA"},
                {"RES_CAPTA", "CAPTA"}, {"RES_OFAC_FSE", "OFAC FSE"}, {"RES_UK", "UK Sanctions"},
                {"RES_OFAC_NSISA", "OFAC NS-ISA"}, {"RES_OFAC_SDN", "OFAC SDN"},
                {"RES_OFAC_NONSDN", "OFAC Non-SDN"}, {"RES_OFAC_SSI", "OFAC SSI"}};
        String[][] pep = {{"PEP_CO", "PEP Colombia"}, {"PEP_REL", "Relacionados con PEP"},
                {"PEP_FUNC", "Funcionarios públicos"}, {"PEP_ALTOS", "Altos funcionarios"},
                {"PEP_MAG", "Magistrados"}, {"PEP_CARGOS", "Cargos públicos"}, {"PEP_INT", "PEP internacionales"}};
        String[][] background = {{"ANT_PROCURADURIA", "Procuraduría"}, {"ANT_CONTRALORIA", "Contraloría"},
                {"ANT_CONTADURIA", "Contaduría"}, {"ANT_POLICIA", "Policía"}, {"ANT_INSOLVENCIAS", "Insolvencias"},
                {"ANT_JEPMS", "JEPMS"}, {"ANT_OTRAS", "Otras fuentes"}};
        String[][] traffic = {{"TRA_RUNT", "RUNT"}, {"TRA_SIMIT", "SIMIT"}, {"TRA_SIMUR", "SIMUR"},
                {"TRA_RNDC", "RNDC"}};
        String[][] general = {{"GEN_NOTABLE", "Persona notable"}, {"GEN_CONTRATACION", "Contratación pública"},
                {"GEN_NOTICIAS", "Noticias reputacionales"}, {"GEN_INSOLVENCIAS", "Insolvencias (información general)"},
                {"GEN_EMPRESARIAL", "Información empresarial"}};
        sources(binding, "LISTA_VINCULANTE", SourceStatus.NO_REGISTRA);
        sources(restrictive, "LISTA_RESTRICTIVA", SourceStatus.NO_REGISTRA);
        sources(pep, "PEP", SourceStatus.NO_REGISTRA);
        sources(background, "ANTECEDENTES", SourceStatus.NO_REGISTRA);
        for (int i = 0; i < CITIES.length; i++) {
            source(String.format("JUD_%02d", i + 1), "Rama Judicial Simulada — " + CITIES[i], "JUDICIAL",
                    SourceStatus.SIN_HALLAZGOS, CITIES[i], null);
        }
        sources(traffic, "TRANSITO", SourceStatus.SIN_HALLAZGOS);
        sources(general, "GENERAL", SourceStatus.SIN_HALLAZGOS);
    }

    private void sources(String[][] items, String category, SourceStatus emptyStatus) {
        for (String[] item : items) {
            source(item[0], item[1], category, emptyStatus, null, null);
        }
    }

    private void source(String code, String name, String category, SourceStatus emptyStatus, String city,
                        Long bankId) {
        Source source = new Source();
        source.code = code;
        source.name = name;
        source.category = category;
        source.emptyStatus = emptyStatus;
        source.city = city;
        source.bankId = bankId;
        em.persist(source);
        sources.put(code, source);
    }

    private Source judicialSource(String city) {
        return sources.get(String.format("JUD_%02d", Arrays.asList(CITIES).indexOf(city) + 1));
    }

    private void seedPersons() {
        for (int i = 0; i < PERSONS.length; i++) {
            Person person = new Person();
            person.fullName = PERSONS[i][0];
            person.document = String.format("1.000.000.%03d", i + 1);
            person.documentType = "CC ficticia";
            person.city = PERSONS[i][1];
            person.occupation = PERSONS[i][2];
            person.birthDate = LocalDate.parse(PERSONS[i][3]);
            person.overallStatus = DueDiligenceService.STATUS_COMPLETE;
            em.persist(person);
            persons.add(person);
        }
    }

    /** 30 cuentas y 200 movimientos (20 cuentas con 7 y 10 con 6). */
    private void seedAccounts(Random rnd) {
        int[] counts = {8, 3, 3, 3, 2, 2, 2, 2, 3, 2};
        int[] juanBanks = {0, 0, 1, 1, 2, 2, 3, 4};
        int index = 0;
        for (int p = 0; p < persons.size(); p++) {
            for (int k = 0; k < counts[p]; k++, index++) {
                int bankIdx = p == 0 ? juanBanks[k] : (p + 2 * k) % banks.size();
                Account account = new Account();
                account.personId = persons.get(p).id;
                account.bank = banks.get(bankIdx);
                account.number = String.format("40%d%02d%02d%04d", bankIdx + 1, p + 1, k + 1, rnd.nextInt(10000));
                account.type = k % 2 == 0 ? "AHORROS" : "CORRIENTE";
                account.status = index % 11 == 10 ? "INACTIVA" : "ACTIVA";
                account.openedAt = BASE.minusMonths(6 + rnd.nextInt(90));
                BigDecimal opening = thousands(500 + rnd.nextInt(7500));
                List<TransactionGenerator.Tx> txs = TransactionGenerator.generate(opening, index < 20 ? 7 : 6, rnd, BASE_TS);
                account.balance = txs.get(txs.size() - 1).after();
                em.persist(account);
                for (TransactionGenerator.Tx tx : txs) {
                    Transaction t = new Transaction();
                    t.accountId = account.id;
                    t.personId = account.personId;
                    t.txDate = tx.date();
                    t.description = tx.description();
                    t.type = tx.type();
                    t.channel = tx.channel();
                    t.amount = tx.amount();
                    t.balanceBefore = tx.before();
                    t.balanceAfter = tx.after();
                    em.persist(t);
                }
            }
        }
    }

    /** 20 créditos (4 de Juan Pérez, fijos) con 10 cuotas cada uno = 200 pagos. */
    private void seedCredits(Random rnd) {
        Person juan = persons.get(0);
        credit(juan, 0, CREDIT_TYPES[0], 15_000, 8_500, 520, "AL_DIA", BASE.minusMonths(20), rnd);
        credit(juan, 1, CREDIT_TYPES[1], 8_000, 5_200, 410, "EN_MORA", BASE.minusMonths(30), rnd);
        credit(juan, 2, CREDIT_TYPES[2], 12_000, 1_500, 380, "AL_DIA", BASE.minusMonths(34), rnd);
        credit(juan, 3, CREDIT_TYPES[4], 2_000, 600, 120, "REESTRUCTURADO", BASE.minusMonths(14), rnd);

        int[] counts = {2, 2, 2, 2, 2, 2, 2, 1, 1};
        int i = 0;
        for (int p = 1; p < persons.size(); p++) {
            for (int k = 0; k < counts[p - 1]; k++, i++) {
                String status = CREDIT_STATUSES[i];
                long initial = 3_000 + rnd.nextInt(40_000);
                long balance = "CERRADO".equals(status) ? 0 : initial * (20 + rnd.nextInt(60)) / 100;
                credit(persons.get(p), (p + i) % banks.size(), CREDIT_TYPES[i % CREDIT_TYPES.length], initial, balance,
                        Math.max(80, initial / 36), status, BASE.minusMonths(12 + rnd.nextInt(36)), rnd);
            }
        }
    }

    private void credit(Person person, int bankIdx, String[] type, long initial, long balance, long installment,
                        String status, LocalDate openedAt, Random rnd) {
        Credit credit = new Credit();
        credit.personId = person.id;
        credit.bank = banks.get(bankIdx);
        credit.number = String.format("77%d%03d%04d", bankIdx + 1, ++sequence, rnd.nextInt(10000));
        credit.product = type[0];
        credit.type = type[1];
        credit.initialAmount = thousands(initial);
        credit.balance = thousands(balance);
        credit.installment = thousands(installment);
        credit.rate = BigDecimal.valueOf(120 + rnd.nextInt(160), 1).setScale(2);
        credit.termMonths = "TARJETA_CREDITO".equals(type[1]) ? 24 : 36 + 12 * rnd.nextInt(3);
        credit.status = status;
        credit.restructured = "REESTRUCTURADO".equals(status);
        credit.openedAt = openedAt;
        credit.closedAt = "CERRADO".equals(status) ? BASE.minusMonths(1 + rnd.nextInt(6)) : null;
        em.persist(credit);

        // Cuotas mensuales; la última vence el mes anterior a la fecha base.
        int firstUnpaid = "EN_MORA".equals(status) ? 9 : "COBRANZA_JURIDICA".equals(status) ? 6 : 11;
        LocalDate oldestUnpaid = null;
        for (int n = 1; n <= 10; n++) {
            CreditPayment payment = new CreditPayment();
            payment.creditId = credit.id;
            payment.personId = person.id;
            payment.installmentNo = n;
            payment.dueDate = BASE.withDayOfMonth(5).minusMonths(11 - n);
            payment.amount = credit.installment;
            if (n >= firstUnpaid) {
                payment.status = "EN_MORA";
                payment.daysLate = (int) ChronoUnit.DAYS.between(payment.dueDate, BASE);
                oldestUnpaid = oldestUnpaid == null ? payment.dueDate : oldestUnpaid;
            } else if (rnd.nextInt(8) == 0) {
                payment.status = "PAGADO_TARDE";
                payment.daysLate = 3 + rnd.nextInt(18);
                payment.paidDate = payment.dueDate.plusDays(payment.daysLate);
            } else {
                payment.status = "PAGADO";
                payment.paidDate = payment.dueDate.minusDays(rnd.nextInt(4));
            }
            em.persist(payment);
        }
        if (oldestUnpaid != null) {
            credit.daysPastDue = (int) ChronoUnit.DAYS.between(oldestUnpaid, BASE);
        }

        history(credit, openedAt, "APERTURA", "Apertura de " + type[0] + " en " + credit.bank.name + ".");
        if (oldestUnpaid != null) {
            history(credit, oldestUnpaid.plusDays(1), "MORA", "La obligación simulada entra en mora.");
        }
        if ("COBRANZA_JURIDICA".equals(status)) {
            history(credit, BASE.minusMonths(2), "COBRANZA_JURIDICA", "La obligación simulada pasa a cobranza jurídica.");
        }
        if (credit.restructured) {
            history(credit, BASE.minusMonths(4), "REESTRUCTURACION", "Reestructuración simulada: nuevo plazo y cuota.");
        }
        if (credit.closedAt != null) {
            history(credit, credit.closedAt, "CIERRE", "Obligación simulada pagada y cerrada.");
        }
    }

    private void history(Credit credit, LocalDate date, String type, String description) {
        CreditHistory event = new CreditHistory();
        event.creditId = credit.id;
        event.personId = credit.personId;
        event.eventDate = date;
        event.eventType = type;
        event.description = description;
        em.persist(event);
    }

    private void seedScores(Random rnd) {
        for (int p = 0; p < persons.size(); p++) {
            CreditScore score = new CreditScore();
            score.personId = persons.get(p).id;
            score.maxScore = 900;
            score.calculatedAt = BASE_TS;
            if (p == 0) {
                score.score = 742;
                score.paymentHistory = 82;
                score.debtLevel = 68;
                score.cardUtilization = 65;
                score.creditAge = 74;
                score.activeCredits = 70;
                score.delinquency = 55;
            } else {
                score.score = 480 + rnd.nextInt(380);
                score.paymentHistory = 40 + rnd.nextInt(56);
                score.debtLevel = 40 + rnd.nextInt(56);
                score.cardUtilization = 40 + rnd.nextInt(56);
                score.creditAge = 40 + rnd.nextInt(56);
                score.activeCredits = 40 + rnd.nextInt(56);
                score.delinquency = 40 + rnd.nextInt(56);
            }
            em.persist(score);
        }
    }

    /** 15 procesos (2 de Juan Pérez), 10 demandas y 5 medidas cautelares. */
    private void seedJudicial(Random rnd) {
        Person juan = persons.get(0);
        JudicialProcess medellin = process(juan, "Medellín", "Ejecutivo singular", "ACTIVO", MatchType.CONFIRMADA,
                true, "Banco Andino (ficticio)", rnd);
        process(juan, "Bogotá", "Verbal sumario", "TERMINADO", MatchType.CONFIRMADA, false, PLAINTIFFS[2], rnd);
        lawsuit(juan, medellin, 5_200, rnd);
        measure(juan, medellin, "EMBARGO", "Cuenta de ahorros en banco ficticio", 5_200);

        int[] counts = {2, 1, 2, 1, 2, 1, 2, 1, 1};
        String[] measures = {"EMBARGO", "SECUESTRO", "INSCRIPCION_DEMANDA", "EMBARGO"};
        int i = 0;
        for (int p = 1; p < persons.size(); p++) {
            Person person = persons.get(p);
            for (int k = 0; k < counts[p - 1]; k++, i++) {
                String type = PROCESS_TYPES[i % PROCESS_TYPES.length];
                MatchType match = i % 4 == 3 ? MatchType.NOMINAL : MatchType.CONFIRMADA;
                JudicialProcess process = process(person, CITIES[(p * 3 + k * 5) % CITIES.length], type,
                        PROCESS_STATUSES[i % PROCESS_STATUSES.length], match, i % 4 == 0,
                        PLAINTIFFS[i % PLAINTIFFS.length], rnd);
                if (k == 0) {
                    lawsuit(person, process, 2_000 + rnd.nextInt(30_000), rnd);
                    if (p % 2 == 0) {
                        measure(person, process, measures[p / 2 - 1], p % 4 == 0 ? "Vehículo ficticio de placas SIM-" + (100 + p)
                                : "Cuenta de ahorros en banco ficticio", 1_000 + rnd.nextInt(9_000));
                    }
                }
            }
        }
    }

    private JudicialProcess process(Person person, String city, String type, String status, MatchType match,
                                    boolean legalCollection, String plaintiff, Random rnd) {
        JudicialProcess process = new JudicialProcess();
        process.personId = person.id;
        process.source = judicialSource(city);
        process.processNumber = String.format("SIM-%d-%05d-%02d", 2024 + rnd.nextInt(3), ++sequence, rnd.nextInt(100));
        process.processType = type;
        process.court = "Juzgado " + (1 + rnd.nextInt(30)) + " Civil Municipal Ficticio de " + city;
        process.city = city;
        process.plaintiff = plaintiff;
        // Una coincidencia nominal apunta a un homónimo ficticio: el nombre se parece, el documento no coincide.
        process.defendant = match == MatchType.NOMINAL ? person.fullName.toUpperCase() + " (homónimo)" : person.fullName;
        process.status = status;
        process.filedAt = BASE.minusDays(120 + rnd.nextInt(600));
        process.lastActionDate = BASE.minusDays(2 + rnd.nextInt(90));
        process.lastAction = switch (status) {
            case "ACTIVO" -> "Auto que libra mandamiento de pago (simulado)";
            case "TERMINADO" -> "Sentencia ejecutoriada (simulada)";
            default -> "Archivo definitivo del expediente (simulado)";
        };
        process.matchType = match;
        process.legalCollection = legalCollection;
        em.persist(process);
        return process;
    }

    private void lawsuit(Person person, JudicialProcess process, long amount, Random rnd) {
        Lawsuit lawsuit = new Lawsuit();
        lawsuit.personId = person.id;
        lawsuit.processId = process.id;
        lawsuit.lawsuitNumber = String.format("DEM-SIM-%05d", ++sequence);
        lawsuit.claimType = "Demanda — " + process.processType;
        lawsuit.plaintiff = process.plaintiff;
        lawsuit.defendant = process.defendant;
        lawsuit.amount = thousands(amount);
        lawsuit.status = "ACTIVO".equals(process.status) ? "ADMITIDA" : "TERMINADA";
        lawsuit.filedAt = process.filedAt.minusDays(5 + rnd.nextInt(20));
        em.persist(lawsuit);
    }

    private void measure(Person person, JudicialProcess process, String type, String asset, long amount) {
        LegalMeasure measure = new LegalMeasure();
        measure.personId = person.id;
        measure.processId = process.id;
        measure.measureType = type;
        measure.asset = asset;
        measure.amount = thousands(amount);
        measure.status = "ACTIVO".equals(process.status) ? "VIGENTE" : "LEVANTADA";
        measure.orderedAt = process.filedAt.plusDays(30);
        em.persist(measure);
    }

    private void seedListsAndRecords() {
        sanction(3, "RES_OFAC_SDN", MatchType.NOMINAL, "ANA MARTINEZ R.", "Programa simulado SDN-X");
        sanction(6, "RES_PANAMA", MatchType.CONFIRMADA, "Andrés Ramírez", "Sociedad offshore ficticia");
        sanction(8, "VIN_UN_SC", MatchType.NOMINAL, "DIEGO MORALES", "Resolución simulada 0000");
        sanction(4, "RES_WB", MatchType.CONFIRMADA, "Luis Hernández", "Inhabilitación simulada de contratista");

        pep(0, "PEP_INT", MatchType.NOMINAL, "JUAN PEREZ", "Viceministro (país ficticio)", "Gobierno de Ficticia");
        pep(2, "PEP_FUNC", MatchType.CONFIRMADA, "Carlos Rodríguez", "Director administrativo", "Alcaldía Simulada");
        pep(5, "PEP_REL", MatchType.NOMINAL, "LAURA TORRES", "Familiar de concejal ficticio", "Concejo Simulado");

        background(4, "ANT_CONTRALORIA", MatchType.CONFIRMADA, "Responsabilidad fiscal simulada", "VIGENTE");
        background(7, "ANT_INSOLVENCIAS", MatchType.CONFIRMADA, "Trámite de insolvencia simulado", "EN_TRAMITE");
        background(1, "ANT_PROCURADURIA", MatchType.NOMINAL, "Sanción disciplinaria simulada", "CERRADO");

        traffic(0, "TRA_SIMIT", "COMPARENDO", "Bogotá", 200, 468, "PAGADO", MatchType.CONFIRMADA);
        traffic(1, "TRA_SIMIT", "MULTA", "Medellín", 90, 936, "PENDIENTE", MatchType.CONFIRMADA);
        traffic(2, "TRA_SIMUR", "COMPARENDO", "Cali", 40, 468, "PENDIENTE", MatchType.CONFIRMADA);
        traffic(2, "TRA_SIMIT", "ACUERDO_PAGO", "Cali", 20, 1_200, "VIGENTE", MatchType.CONFIRMADA);
        traffic(5, "TRA_RUNT", "MULTA", "Bucaramanga", 300, 234, "PAGADO", MatchType.CONFIRMADA);
        traffic(6, "TRA_SIMIT", "COMPARENDO", "Pereira", 15, 468, "PENDIENTE", MatchType.NOMINAL);
        traffic(8, "TRA_RNDC", "MULTA", "Ibagué", 60, 1_872, "PENDIENTE", MatchType.CONFIRMADA);
        traffic(9, "TRA_SIMIT", "ACUERDO_PAGO", "Santa Marta", 150, 700, "INCUMPLIDO", MatchType.CONFIRMADA);

        news(0, "GEN_NOTICIAS", "Empresa ficticia vinculada a investigación administrativa", "NOTICIA", "MEDIO",
                MatchType.NOMINAL, "Noticias Simuladas", 45);
        news(2, "GEN_CONTRATACION", "Contrato simulado de suministro con entidad ficticia", "CONTRATACION", "BAJO",
                MatchType.CONFIRMADA, "Portal de Contratación Simulado", 120);
        news(3, "GEN_NOTICIAS", "Comerciante ficticia mencionada en nota sobre contrabando", "NOTICIA", "MEDIO",
                MatchType.NOMINAL, "El Diario Imaginario", 30);
        news(4, "GEN_CONTRATACION", "Contratista ficticio con contrato simulado terminado por incumplimiento",
                "CONTRATACION", "ALTO", MatchType.CONFIRMADA, "Portal de Contratación Simulado", 200);
        news(6, "GEN_EMPRESARIAL", "Representante legal de Comercializadora Imaginaria S.A.S.", "EMPRESARIAL", "BAJO",
                MatchType.CONFIRMADA, "Registro Empresarial Simulado", 400);
        news(6, "GEN_NOTABLE", "Empresario ficticio reconocido en premio regional inventado", "PERSONA_NOTABLE", "BAJO",
                MatchType.CONFIRMADA, "Gaceta Ficticia", 250);
        news(7, "GEN_INSOLVENCIAS", "Admisión simulada a trámite de insolvencia de persona natural", "INSOLVENCIA",
                "MEDIO", MatchType.CONFIRMADA, "Boletín Concursal Simulado", 75);
        news(8, "GEN_NOTICIAS", "Transportador ficticio citado en reportaje sobre accidentalidad", "NOTICIA", "BAJO",
                MatchType.NOMINAL, "Noticias Simuladas", 10);
    }

    private void sanction(int p, String code, MatchType match, String name, String program) {
        Sanction s = new Sanction();
        s.personId = persons.get(p).id;
        s.source = sources.get(code);
        s.listType = s.source.category;
        s.matchedName = name;
        s.matchType = match;
        s.program = program;
        s.listedAt = BASE.minusDays(300L + 40 * p);
        s.detail = "Registro simulado en " + s.source.name + ".";
        em.persist(s);
    }

    private void pep(int p, String code, MatchType match, String name, String position, String entity) {
        PepRecord r = new PepRecord();
        r.personId = persons.get(p).id;
        r.source = sources.get(code);
        r.matchedName = name;
        r.position = position;
        r.entity = entity;
        r.matchType = match;
        r.fromDate = BASE.minusYears(4);
        r.toDate = match == MatchType.CONFIRMADA ? null : BASE.minusYears(1);
        r.detail = "Registro PEP simulado en " + r.source.name + ".";
        em.persist(r);
    }

    private void background(int p, String code, MatchType match, String type, String status) {
        BackgroundCheck b = new BackgroundCheck();
        b.personId = persons.get(p).id;
        b.source = sources.get(code);
        b.recordType = type;
        b.reference = String.format("ANT-SIM-%05d", ++sequence);
        b.matchedName = match == MatchType.NOMINAL ? persons.get(p).fullName.toUpperCase() : persons.get(p).fullName;
        b.matchType = match;
        b.recordDate = BASE.minusDays(200L + 30 * p);
        b.status = status;
        b.detail = "Antecedente simulado en " + b.source.name + ".";
        em.persist(b);
    }

    private void traffic(int p, String code, String type, String city, int daysAgo, long amount, String status,
                         MatchType match) {
        TrafficRecord t = new TrafficRecord();
        t.personId = persons.get(p).id;
        t.source = sources.get(code);
        t.recordType = type;
        t.reference = String.format("TRA-SIM-%05d", ++sequence);
        t.city = city;
        t.recordDate = BASE.minusDays(daysAgo);
        t.amount = thousands(amount);
        t.status = status;
        t.matchType = match;
        em.persist(t);
    }

    private void news(int p, String code, String title, String category, String level, MatchType match,
                      String outlet, int daysAgo) {
        ReputationalNews n = new ReputationalNews();
        n.personId = persons.get(p).id;
        n.source = sources.get(code);
        n.title = title;
        n.publishedAt = BASE.minusDays(daysAgo);
        n.outlet = outlet;
        n.category = category;
        n.level = level;
        n.status = match == MatchType.CONFIRMADA ? "CONFIRMADA" : "POR_VERIFICAR";
        n.matchType = match;
        em.persist(n);
    }

    private <T> List<T> of(Class<T> type, Person person) {
        return em.createQuery("select e from " + type.getSimpleName() + " e where e.personId = :p order by e.id", type)
                .setParameter("p", person.id).getResultList();
    }

    /** Exactamente 3 alertas por persona, derivadas de sus datos ficticios (30 en total). */
    private void seedAlerts(Person person) {
        List<String[]> found = new ArrayList<>();
        boolean nominal = false;
        for (JudicialProcess j : of(JudicialProcess.class, person)) {
            if (j.matchType == MatchType.NOMINAL) {
                nominal = true;
            } else if ("ACTIVO".equals(j.status)) {
                found.add(new String[]{"ALTO", "Proceso judicial simulado activo",
                        "Proceso " + j.processNumber + " (" + j.processType + ") en " + j.city + ".", "JUDICIAL"});
            }
        }
        for (Credit c : of(Credit.class, person)) {
            if (c.daysPastDue > 0) {
                found.add(new String[]{"MEDIO", "Obligación crediticia simulada en mora",
                        c.product + " en " + c.bank.name + " con " + c.daysPastDue + " días de mora.", "CREDITO"});
            }
        }
        for (Sanction s : of(Sanction.class, person)) {
            if (s.matchType == MatchType.NOMINAL) {
                nominal = true;
            } else {
                found.add(new String[]{"ALTO", "Coincidencia confirmada en lista simulada",
                        "Registro confirmado por documento en " + s.source.name + ".", "LISTAS"});
            }
        }
        for (PepRecord r : of(PepRecord.class, person)) {
            if (r.matchType == MatchType.NOMINAL) {
                nominal = true;
            } else {
                found.add(new String[]{"MEDIO", "Persona expuesta políticamente (simulada)",
                        r.position + " en " + r.entity + ".", "PEP"});
            }
        }
        for (BackgroundCheck b : of(BackgroundCheck.class, person)) {
            if (b.matchType == MatchType.NOMINAL) {
                nominal = true;
            } else {
                found.add(new String[]{"MEDIO", "Antecedente simulado confirmado", b.recordType + " en " + b.source.name + ".",
                        "ANTECEDENTES"});
            }
        }
        for (ReputationalNews n : of(ReputationalNews.class, person)) {
            nominal = nominal || n.matchType == MatchType.NOMINAL;
        }
        if (nominal) {
            // La coincidencia nominal siempre queda entre las tres alertas: es la que más se presta a confusión.
            String[] low = {"BAJO", "Coincidencia nominal simulada",
                    "Coincidencia solo por nombre; puede corresponder a un homónimo ficticio.", "HOMONIMOS"};
            found.add(Math.min(2, found.size()), low);
        }
        String[] empty = {"Listas", "Antecedentes", "Tránsito"};
        for (int i = 0; i < 3; i++) {
            String[] item = i < found.size() ? found.get(i) : new String[]{"SIN_HALLAZGOS", "Sin hallazgos en " + empty[i],
                    "No se encontraron registros simulados.", empty[i].toUpperCase()};
            Alert alert = new Alert();
            alert.personId = person.id;
            alert.severity = Severity.valueOf(item[0]);
            alert.title = item[1];
            alert.description = item[2];
            alert.category = item[3];
            alert.createdAt = BASE_TS.minusDays(i + 1L).minusHours(i * 3L);
            alert.status = "ABIERTA";
            em.persist(alert);
        }
    }

    private void seedTimeline(Person person) {
        String[][] events = {{"MOVIMIENTO", "Nuevo movimiento bancario"}, {"PAGO_CREDITO", "Pago de crédito"},
                {"OBLIGACION", "Cambio en obligación"}, {"PROCESO", "Actualización de proceso judicial simulado"},
                {"ALERTA", "Nueva alerta"}};
        for (int i = 0; i < events.length; i++) {
            TimelineEvent event = new TimelineEvent();
            event.personId = person.id;
            event.eventType = events[i][0];
            event.description = events[i][1];
            event.occurredAt = BASE_TS.minusHours(2 + i * 19L);
            em.persist(event);
        }
    }
}
