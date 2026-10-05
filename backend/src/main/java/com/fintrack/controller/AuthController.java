package com.fintrack.controller;

import com.fintrack.audit.AuditService;
import com.fintrack.dto.LoginRequest;
import com.fintrack.dto.LoginResponse;
import com.fintrack.entity.AppUser;
import com.fintrack.exception.UnauthorizedException;
import com.fintrack.repository.AppUserRepository;
import com.fintrack.security.JwtService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final AuditService audit;

    public AuthController(AppUserRepository users, PasswordEncoder encoder, JwtService jwt, AuditService audit) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.audit = audit;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AppUser user = users.findByUsername(request.username().trim())
                .filter(u -> encoder.matches(request.password(), u.passwordHash))
                .orElseThrow(() -> new UnauthorizedException("Usuario o contraseña incorrectos."));
        audit.log("LOGIN", "Inicio de sesión de " + user.username);
        return new LoginResponse(jwt.generate(user.username, user.role, user.personId), user.username, user.fullName, user.role);
    }

    @GetMapping("/me")
    public AppUser me(Principal principal) {
        return users.findByUsername(principal.getName())
                .orElseThrow(() -> new UnauthorizedException("Sesión no válida."));
    }
}
