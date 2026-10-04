package com.fintrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/** FinTrack 360 — PROTOTIPO EDUCATIVO. Todos los datos son simulados; no hay conexión con fuentes reales. */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class FinTrackApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinTrackApplication.class, args);
    }
}
