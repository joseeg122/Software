package com.fintrack.seed;

import com.fintrack.service.BalanceCalculator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Genera movimientos ficticios deterministas con saldos encadenados. */
public final class TransactionGenerator {
    private static final String[] CREDITS = {"Nómina simulada", "Transferencia recibida", "Consignación en oficina",
            "Rendimientos financieros"};
    private static final String[] DEBITS = {"Compra en comercio ficticio", "Pago de servicios públicos",
            "Retiro en cajero", "Transferencia enviada", "Pago de cuota de crédito"};
    private static final String[] CHANNELS = {"App móvil", "Cajero", "Oficina", "Portal web", "Datáfono"};

    public record Tx(LocalDateTime date, String description, String type, String channel, BigDecimal amount,
                     BigDecimal before, BigDecimal after) {
    }

    private TransactionGenerator() {
    }

    /** Los movimientos quedan en orden cronológico y terminan antes de {@code end}. */
    public static List<Tx> generate(BigDecimal opening, int count, Random rnd, LocalDateTime end) {
        List<Tx> out = new ArrayList<>();
        BigDecimal balance = opening;
        LocalDateTime date = end.minusDays(count * 4L + 1);
        for (int i = 0; i < count; i++) {
            date = date.plusDays(1 + rnd.nextInt(4)).withHour(8 + rnd.nextInt(10)).withMinute(rnd.nextInt(60));
            boolean debit = rnd.nextInt(10) < 6;
            BigDecimal amount = thousands(debit ? 20 + rnd.nextInt(880) : 200 + rnd.nextInt(2800));
            // Una cuenta ficticia no queda en negativo: si el débito no cabe, se registra un abono.
            if (debit && amount.compareTo(balance) > 0) {
                debit = false;
            }
            String type = debit ? BalanceCalculator.DEBITO : BalanceCalculator.CREDITO;
            String[] pool = debit ? DEBITS : CREDITS;
            BigDecimal after = BalanceCalculator.after(balance, type, amount);
            out.add(new Tx(date, pool[rnd.nextInt(pool.length)], type, CHANNELS[rnd.nextInt(CHANNELS.length)], amount,
                    balance, after));
            balance = after;
        }
        return out;
    }

    public static BigDecimal thousands(long value) {
        return BigDecimal.valueOf(value * 1000).setScale(2);
    }
}
