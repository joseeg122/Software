package com.fintrack;

import static org.junit.jupiter.api.Assertions.*;

import com.fintrack.entity.Masking;
import com.fintrack.entity.MatchType;
import com.fintrack.entity.Source;
import com.fintrack.entity.SourceStatus;
import com.fintrack.exception.BadRequestException;
import com.fintrack.mock.SourceSimulator;
import com.fintrack.mock.SourceSimulator.Outcome;
import com.fintrack.seed.TransactionGenerator;
import com.fintrack.seed.TransactionGenerator.Tx;
import com.fintrack.service.BalanceCalculator;
import com.fintrack.service.InputGuard;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Reglas no negociables del proyecto, sin levantar Spring ni base de datos. */
class RulesTest {
    private final SourceSimulator simulator = new SourceSimulator();

    private static Source source(String code, String category, SourceStatus emptyStatus) {
        Source s = new Source();
        s.code = code;
        s.name = code;
        s.category = category;
        s.emptyStatus = emptyStatus;
        return s;
    }

    @Test
    void saldoPosteriorEsSaldoAnteriorMasOMenosValor() {
        BigDecimal before = new BigDecimal("1000.00");
        assertEquals(new BigDecimal("1250.50"), BalanceCalculator.after(before, "CREDITO", new BigDecimal("250.50")));
        assertEquals(new BigDecimal("749.50"), BalanceCalculator.after(before, "DEBITO", new BigDecimal("250.50")));
        assertThrows(IllegalArgumentException.class, () -> BalanceCalculator.after(before, "DEBITO", BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> BalanceCalculator.after(before, "OTRO", BigDecimal.ONE));
    }

    @Test
    void movimientosGeneradosEncadenanSaldosYSonDeterministas() {
        LocalDateTime end = LocalDateTime.of(2026, 10, 3, 21, 0);
        BigDecimal opening = new BigDecimal("900000.00");
        List<Tx> txs = TransactionGenerator.generate(opening, 50, new Random(7), end);
        BigDecimal balance = opening;
        for (Tx tx : txs) {
            assertEquals(balance, tx.before());
            BigDecimal delta = "CREDITO".equals(tx.type()) ? tx.amount() : tx.amount().negate();
            assertEquals(tx.before().add(delta), tx.after());
            assertTrue(tx.after().signum() >= 0, "una cuenta ficticia no queda en negativo");
            assertFalse(tx.date().isAfter(end));
            balance = tx.after();
        }
        assertEquals(txs, TransactionGenerator.generate(opening, 50, new Random(7), end));
    }

    @Test
    void cuentaSiempreEnmascarada() {
        assertEquals("****4582", Masking.mask("4010101014582"));
        assertEquals("****", Masking.mask(null));
        assertEquals("****12", Masking.mask("12"));
    }

    @Test
    void unErrorOFuenteNoDisponibleNuncaEsSinHallazgos() {
        Source s = source("FUENTE_X", "ANTECEDENTES", SourceStatus.SIN_HALLAZGOS);
        int failures = 0;
        for (long person = 1; person <= 10; person++) {
            for (int run = 0; run < 200; run++) {
                // La fuente falla aunque NO existan registros: el resultado debe seguir siendo un fallo.
                Outcome o = simulator.query(s, person, run, List.of(), false, false);
                if (o.status().isFailure()) {
                    failures++;
                    assertEquals(0, o.matches());
                    assertTrue(o.summary().contains("no equivale"));
                } else {
                    assertEquals(SourceStatus.SIN_HALLAZGOS, o.status());
                }
            }
        }
        assertTrue(failures > 0, "el simulador debe producir fallos");
    }

    @Test
    void coincidenciaPorNombreNoEsCoincidenciaConfirmada() {
        Source s = source("FUENTE_Y", "PEP", SourceStatus.NO_REGISTRA);
        Outcome nominal = simulator.query(s, 1, 0, List.of(MatchType.NOMINAL, MatchType.NOMINAL), false, true);
        assertEquals(SourceStatus.HALLAZGO, nominal.status());
        assertEquals(MatchType.NOMINAL, nominal.matchType());
        assertEquals(2, nominal.matches());

        Outcome confirmed = simulator.query(s, 1, 0, List.of(MatchType.NOMINAL, MatchType.CONFIRMADA), false, true);
        assertEquals(MatchType.CONFIRMADA, confirmed.matchType());

        Outcome empty = simulator.query(s, 1, 0, List.of(), false, true);
        assertEquals(SourceStatus.NO_REGISTRA, empty.status());
        assertEquals(MatchType.NINGUNA, empty.matchType());
    }

    @Test
    void bancoConectadoSoloSiRespondeYTieneProductos() {
        Source bank = source("banco-x", "BANCO", SourceStatus.NO_REGISTRA);
        assertEquals(SourceStatus.DISPONIBLE, simulator.query(bank, 1, 0, List.of(), true, true).status());
        assertEquals(SourceStatus.NO_REGISTRA, simulator.query(bank, 1, 0, List.of(), false, true).status());
    }

    @Test
    void textoLibreNoAdmiteDocumentosNiSecretos() {
        assertEquals("Revisar el proceso simulado.", InputGuard.check("  Revisar el proceso simulado. "));
        assertThrows(BadRequestException.class, () -> InputGuard.check("Su cédula es 1.032.456.789"));
        assertThrows(BadRequestException.class, () -> InputGuard.check("cuenta 4010 1010 1458"));
        assertThrows(BadRequestException.class, () -> InputGuard.check("la contraseña del banco es abc"));
        assertThrows(BadRequestException.class, () -> InputGuard.check("   "));
    }
}
