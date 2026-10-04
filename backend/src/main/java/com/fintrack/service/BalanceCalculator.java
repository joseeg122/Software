package com.fintrack.service;

import java.math.BigDecimal;

public final class BalanceCalculator {
    public static final String CREDITO = "CREDITO";
    public static final String DEBITO = "DEBITO";

    private BalanceCalculator() {
    }

    /** Saldo posterior = saldo anterior + valor (CREDITO) o − valor (DEBITO). */
    public static BigDecimal after(BigDecimal before, String type, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("El valor del movimiento debe ser positivo.");
        }
        return switch (type) {
            case CREDITO -> before.add(amount);
            case DEBITO -> before.subtract(amount);
            default -> throw new IllegalArgumentException("Tipo de movimiento desconocido: " + type);
        };
    }
}
