package com.fintrack.service;

import com.fintrack.exception.BadRequestException;
import java.util.regex.Pattern;

/** Impide escribir en texto libre algo que parezca un documento, cuenta, contraseña o token. */
public final class InputGuard {
    private static final Pattern LONG_NUMBER = Pattern.compile("(\\d[\\s.\\-]?){6,}");
    private static final Pattern SECRET_WORD = Pattern.compile(
            "(?i)(contrase[ñn]a|password|passwd|clave\\s+(bancaria|din[aá]mica|de\\s+acceso)|token|cvv|\\bpin\\b)");

    private InputGuard() {
    }

    public static String check(String text) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            throw new BadRequestException("El texto no puede estar vacío.");
        }
        if (LONG_NUMBER.matcher(value).find() || SECRET_WORD.matcher(value).find()) {
            throw new BadRequestException("Este prototipo no admite documentos, números de cuenta, contraseñas "
                    + "ni tokens. Escribe solo texto descriptivo sobre datos ficticios.");
        }
        return value;
    }
}
