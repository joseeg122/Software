package com.fintrack.ai;

import com.fintrack.dto.AiResponse;
import com.fintrack.dto.ProfileSnapshot;
import com.fintrack.entity.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * FinTrack AI: motor local de reglas. No consulta ninguna fuente ni servicio externo; solo lee el perfil
 * ficticio que el backend ya procesó. Si la pregunta no se puede responder con esos datos, lo dice.
 */
@Component
public class LocalAiEngine {
    public static final String ENGINE = "Motor local de reglas (sin servicios externos)";
    public static final String NOTICE = "Respuesta generada solo con datos simulados del sistema. "
            + "La interpretación es automática y no constituye un concepto real.";

    public AiResponse answer(String question, ProfileSnapshot p) {
        String q = normalize(question);
        List<String> data = new ArrayList<>();
        String interpretation;

        if (q.contains("ejecutivo") || q.contains("resum") || q.contains("perfil")) {
            general(p, data);
            credits(p, data, false);
            processes(p, data);
            findings(p, data);
            unavailable(p, data);
            interpretation = overall(p);
        } else if (q.contains("mora") || q.contains("vencid") || q.contains("atras")) {
            int n = delinquent(p, data);
            interpretation = n == 0
                    ? "Con los datos del sistema no se observan obligaciones simuladas en mora."
                    : "Hay " + n + " obligación(es) simulada(s) en mora; conviene revisarlas antes de continuar.";
        } else if (q.contains("credito") || q.contains("deuda") || q.contains("obligacion") || q.contains("tarjeta")) {
            credits(p, data, true);
            interpretation = "La deuda simulada vigente es " + money(p.dashboard().totalDebt()) + " en "
                    + p.dashboard().activeCredits() + " crédito(s) activo(s).";
        } else if (q.contains("proceso") || q.contains("judicial") || q.contains("demanda")
                || q.contains("embargo") || q.contains("cautelar")) {
            processes(p, data);
            interpretation = judicialReading(p);
        } else if (q.contains("hallazgo") || q.contains("alerta") || q.contains("riesgo")) {
            findings(p, data);
            unavailable(p, data);
            interpretation = overall(p);
        } else if (q.contains("score") || q.contains("puntaje")) {
            score(p, data);
            interpretation = "El score es completamente simulado y no representa un puntaje crediticio real.";
        } else if (q.contains("banco") || q.contains("cuenta") || q.contains("saldo")) {
            banks(p, data);
            interpretation = "Los saldos provienen de bancos ficticios; un banco sin respuesta no implica ausencia de productos.";
        } else if (q.contains("lista") || q.contains("sancion") || q.contains("pep") || q.contains("antecedente")) {
            lists(p, data);
            interpretation = "Las coincidencias solo por nombre pueden corresponder a otra persona ficticia con "
                    + "nombres similares; solo las confirmadas por documento se atribuyen a esta persona.";
        } else if (q.contains("fuente") || q.contains("disponible") || q.contains("error")) {
            unavailable(p, data);
            interpretation = p.unavailableSources().isEmpty()
                    ? "Todas las fuentes simuladas respondieron en la última consulta."
                    : "Las fuentes sin respuesta tienen resultado desconocido: no deben leerse como \"sin hallazgos\".";
        } else {
            data.add("No hay datos del sistema que respondan esa pregunta.");
            interpretation = "Puedo responder sobre: resumen del perfil, créditos, mora, procesos judiciales, "
                    + "hallazgos, score, bancos y cuentas, listas/PEP/antecedentes y fuentes no disponibles.";
        }
        return new AiResponse(question, data, interpretation, ENGINE, NOTICE);
    }

    private void general(ProfileSnapshot p, List<String> data) {
        var d = p.dashboard();
        data.add("Persona ficticia: " + d.fullName() + ", documento ficticio " + d.document() + ".");
        data.add("Estado general: " + d.overallStatus() + ".");
        data.add("Bancos conectados: " + d.banksConnected() + ". Cuentas: " + d.accounts() + ".");
        if (d.score() != null) {
            data.add("Score simulado: " + d.score() + " / " + d.maxScore() + ".");
        }
    }

    private void credits(ProfileSnapshot p, List<String> data, boolean detail) {
        var d = p.dashboard();
        data.add("Créditos registrados: " + p.credits().size() + " (" + d.activeCredits() + " activos). Deuda vigente: "
                + money(d.totalDebt()) + ".");
        if (detail) {
            for (Credit c : p.credits()) {
                data.add(c.bank.name + " — " + c.product + ": saldo " + money(c.balance) + " de "
                        + money(c.initialAmount) + ", estado " + label(c.status)
                        + (c.daysPastDue > 0 ? ", " + c.daysPastDue + " días de mora" : "") + ".");
            }
        }
    }

    private int delinquent(ProfileSnapshot p, List<String> data) {
        List<Credit> late = p.credits().stream().filter(c -> c.daysPastDue > 0).toList();
        if (late.isEmpty()) {
            data.add("Obligaciones en mora: ninguna en los datos simulados.");
        }
        for (Credit c : late) {
            data.add(c.bank.name + " — " + c.product + ": " + c.daysPastDue + " días de mora, saldo "
                    + money(c.balance) + ", estado " + label(c.status) + ".");
        }
        return late.size();
    }

    private void processes(ProfileSnapshot p, List<String> data) {
        if (p.judicialProcesses().isEmpty()) {
            data.add("Procesos judiciales simulados: ninguno registrado.");
        }
        for (JudicialProcess j : p.judicialProcesses()) {
            data.add("Proceso simulado " + j.processNumber + " (" + j.processType + ", " + j.city + "): estado "
                    + label(j.status) + ", " + match(j.matchType) + ".");
        }
        if (!p.lawsuits().isEmpty() || !p.legalMeasures().isEmpty()) {
            data.add("Demandas simuladas: " + p.lawsuits().size() + ". Medidas cautelares simuladas: "
                    + p.legalMeasures().size() + ".");
        }
    }

    private void findings(ProfileSnapshot p, List<String> data) {
        for (Alert a : p.alerts()) {
            data.add("[" + label(a.severity.name()) + "] " + a.title + ": " + a.description);
        }
        if (p.alerts().isEmpty()) {
            data.add("No hay alertas registradas para este perfil.");
        }
    }

    private void score(ProfileSnapshot p, List<String> data) {
        CreditScore s = p.score();
        if (s == null) {
            data.add("No hay score simulado registrado.");
            return;
        }
        data.add("Score simulado: " + s.score + " / " + s.maxScore + ".");
        data.add("Factores (0-100): historial de pagos " + s.paymentHistory + ", endeudamiento " + s.debtLevel
                + ", utilización de tarjetas " + s.cardUtilization + ", antigüedad " + s.creditAge
                + ", créditos activos " + s.activeCredits + ", mora " + s.delinquency + ".");
    }

    private void banks(ProfileSnapshot p, List<String> data) {
        BigDecimal total = p.accounts().stream().map(a -> a.balance).reduce(BigDecimal.ZERO, BigDecimal::add);
        data.add("Bancos conectados: " + p.dashboard().banksConnected() + ". Cuentas: " + p.accounts().size()
                + ". Saldo total simulado: " + money(total) + ".");
        for (Account a : p.accounts()) {
            data.add(a.bank.name + " — " + label(a.type) + " " + a.getMaskedNumber() + ": " + money(a.balance)
                    + " (" + label(a.status) + ").");
        }
    }

    private void lists(ProfileSnapshot p, List<String> data) {
        for (Sanction s : p.sanctions()) {
            data.add("Lista simulada " + s.source.name + ": " + match(s.matchType) + " (" + s.matchedName + ").");
        }
        for (PepRecord r : p.pepRecords()) {
            data.add("PEP simulado " + r.source.name + ": " + match(r.matchType) + " (" + r.matchedName + ").");
        }
        for (BackgroundCheck b : p.backgroundChecks()) {
            data.add("Antecedente simulado " + b.source.name + ": " + match(b.matchType) + " (" + b.recordType + ").");
        }
        if (p.sanctions().isEmpty() && p.pepRecords().isEmpty() && p.backgroundChecks().isEmpty()) {
            data.add("Listas, PEP y antecedentes simulados: sin registros para este perfil.");
        }
        unavailable(p, data);
    }

    private void unavailable(ProfileSnapshot p, List<String> data) {
        if (p.unavailableSources().isEmpty()) {
            return;
        }
        data.add("Fuentes sin respuesta en la última consulta (" + p.unavailableSources().size() + "): "
                + String.join(", ", p.unavailableSources().stream()
                .map(r -> r.source.name + " [" + label(r.status.name()) + "]").toList()) + ".");
    }

    private String judicialReading(ProfileSnapshot p) {
        long active = p.judicialProcesses().stream()
                .filter(j -> "ACTIVO".equals(j.status) && j.matchType == MatchType.CONFIRMADA).count();
        long nominal = p.judicialProcesses().stream().filter(j -> j.matchType == MatchType.NOMINAL).count();
        String text = active == 0 ? "No hay procesos simulados activos confirmados por documento."
                : "Hay " + active + " proceso(s) simulado(s) activo(s) confirmado(s) por documento.";
        return nominal == 0 ? text : text + " " + nominal + " coincidencia(s) son solo por nombre y pueden ser homónimos.";
    }

    private String overall(ProfileSnapshot p) {
        long high = p.alerts().stream().filter(a -> a.severity == Severity.ALTO).count();
        long medium = p.alerts().stream().filter(a -> a.severity == Severity.MEDIO).count();
        long low = p.alerts().stream().filter(a -> a.severity == Severity.BAJO).count();
        String level = high > 0 ? "ALTO" : medium > 0 ? "MEDIO" : low > 0 ? "BAJO" : "SIN HALLAZGOS";
        String text = "Nivel de atención sugerido: " + level + " (" + high + " alto, " + medium + " medio, " + low
                + " bajo).";
        if (!p.unavailableSources().isEmpty()) {
            text += " Hay " + p.unavailableSources().size()
                    + " fuente(s) sin respuesta: su resultado está pendiente y no equivale a \"sin hallazgos\".";
        }
        return text;
    }

    private static String match(MatchType type) {
        return switch (type) {
            case CONFIRMADA -> "coincidencia confirmada por documento";
            case NOMINAL -> "coincidencia solo por nombre (posible homónimo)";
            case NINGUNA -> "sin coincidencia";
        };
    }

    private static String label(String code) {
        return code.replace('_', ' ').toLowerCase(Locale.ROOT);
    }

    private static String money(BigDecimal value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator('.');
        return "$" + new DecimalFormat("#,##0", symbols).format(value);
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
