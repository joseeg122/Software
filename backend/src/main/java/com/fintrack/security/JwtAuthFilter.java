package com.fintrack.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Pattern PERSON_PATH = Pattern.compile("^/api/persons/(\\d+)(/.*)?$");
    private final JwtService jwt;

    public JwtAuthFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwt.parse(header.substring(7));
                var auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class))));
                Number personId = claims.get("personId", Number.class);
                auth.setDetails(personId == null ? null : personId.longValue());
                SecurityContextHolder.getContext().setAuthentication(auth);
                if (personId != null && !allowed(request, personId.longValue())) {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"SIN_ACCESO\",\"message\":\"No tienes acceso a la información de esta persona.\"}");
                    return;
                }
            } catch (JwtException | IllegalArgumentException e) {
                // Token inválido o vencido: la petición sigue sin autenticar y recibirá 401.
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    /** Un usuario ligado a una persona solo puede pedir rutas /api/persons/{su id}/... */
    private static boolean allowed(HttpServletRequest request, long ownId) {
        Matcher m = PERSON_PATH.matcher(request.getRequestURI());
        return !m.matches() || Long.parseLong(m.group(1)) == ownId;
    }
}
