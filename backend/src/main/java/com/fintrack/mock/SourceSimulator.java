package com.fintrack.mock;

import com.fintrack.entity.MatchType;
import com.fintrack.entity.Source;
import com.fintrack.entity.SourceStatus;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Simulador de fuentes externas. NO realiza ninguna conexión de red: decide el resultado de forma
 * determinista a partir de los datos ficticios de la base de datos y del número de consulta.
 */
@Component
public class SourceSimulator {

    public record Outcome(SourceStatus status, MatchType matchType, int matches, String summary) {
    }

    /**
     * @param records        coincidencias ficticias que la fuente tiene para la persona
     * @param hasProducts    solo para bancos: la persona tiene productos en ese banco
     * @param forceAvailable consulta de referencia sin fallos simulados
     */
    public Outcome query(Source source, long personId, int run, List<MatchType> records, boolean hasProducts,
                         boolean forceAvailable) {
        SourceStatus failure = forceAvailable ? null : failure(source.code, personId, run);
        if (failure == SourceStatus.ERROR) {
            return new Outcome(SourceStatus.ERROR, MatchType.NINGUNA, 0,
                    "Error de consulta en la fuente simulada. El resultado es desconocido: no equivale a \"sin hallazgos\".");
        }
        if (failure == SourceStatus.NO_DISPONIBLE) {
            return new Outcome(SourceStatus.NO_DISPONIBLE, MatchType.NINGUNA, 0,
                    "Fuente simulada no disponible. El resultado es desconocido: no equivale a \"sin hallazgos\".");
        }
        if ("BANCO".equals(source.category)) {
            return hasProducts
                    ? new Outcome(SourceStatus.DISPONIBLE, MatchType.NINGUNA, 0, "Banco simulado conectado: productos consultados.")
                    : new Outcome(SourceStatus.NO_REGISTRA, MatchType.NINGUNA, 0, "La persona ficticia no registra productos en este banco simulado.");
        }
        if (records.isEmpty()) {
            String summary = source.emptyStatus == SourceStatus.NO_REGISTRA
                    ? "Fuente simulada consultada: no registra."
                    : "Fuente simulada consultada: sin hallazgos.";
            return new Outcome(source.emptyStatus, MatchType.NINGUNA, 0, summary);
        }
        // Una coincidencia solo por nombre nunca se promueve a confirmada.
        boolean confirmed = records.contains(MatchType.CONFIRMADA);
        int n = records.size();
        String summary = n + (n == 1 ? " coincidencia simulada" : " coincidencias simuladas")
                + (confirmed ? " — confirmada por documento." : " — solo por nombre; puede ser un homónimo.");
        return new Outcome(SourceStatus.HALLAZGO, confirmed ? MatchType.CONFIRMADA : MatchType.NOMINAL, n, summary);
    }

    /** Fallo simulado y determinista: cerca del 4 % de error y del 5 % de indisponibilidad por consulta. */
    static SourceStatus failure(String code, long personId, int run) {
        int h = Math.floorMod(code.hashCode() * 31 + (int) personId * 17 + run * 7919, 100);
        if (h < 4) {
            return SourceStatus.ERROR;
        }
        return h < 9 ? SourceStatus.NO_DISPONIBLE : null;
    }
}
