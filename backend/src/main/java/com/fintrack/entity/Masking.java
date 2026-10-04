package com.fintrack.entity;

public final class Masking {
    private Masking() {
    }

    /** Devuelve el número enmascarado con solo los últimos 4 dígitos visibles (****4582). */
    public static String mask(String number) {
        if (number == null || number.isBlank()) {
            return "****";
        }
        String digits = number.trim();
        return "****" + digits.substring(Math.max(0, digits.length() - 4));
    }
}
