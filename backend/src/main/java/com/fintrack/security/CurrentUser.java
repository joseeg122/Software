package com.fintrack.security;

import com.fintrack.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Acceso al usuario autenticado. Un usuario con persona asignada solo puede ver los datos de esa persona. */
public final class CurrentUser {
    private CurrentUser() {
    }

    /** Id de la única persona visible para el usuario, o null si no tiene restricción. */
    public static Long restrictedPersonId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getDetails() instanceof Long id ? id : null;
    }

    public static void requireAccess(Long personId) {
        Long own = restrictedPersonId();
        if (own != null && !own.equals(personId)) {
            throw new ForbiddenException("No tienes acceso a la información de esta persona.");
        }
    }
}
