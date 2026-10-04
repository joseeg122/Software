package com.fintrack;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.util.UUID;
import org.springframework.boot.SpringApplication;

/**
 * Arranque local SIN Docker: levanta un PostgreSQL embebido y temporal y luego la aplicación.
 * Uso: mvn spring-boot:test-run   (los datos se pierden al cerrar; es solo para demostración).
 */
public class LocalDemoApplication {
    public static void main(String[] args) throws Exception {
        EmbeddedPostgres postgres = EmbeddedPostgres.start();
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl("postgres", "postgres"));
        System.setProperty("spring.datasource.username", "postgres");
        System.setProperty("spring.datasource.password", "");
        // Los secretos salen de variables de entorno; si faltan se generan al azar para esta ejecución.
        if (System.getenv("JWT_SECRET") == null) {
            System.setProperty("app.jwt.secret", UUID.randomUUID() + "-" + UUID.randomUUID());
        }
        if (System.getenv("APP_DEMO_PASSWORD") == null) {
            String password = UUID.randomUUID().toString().substring(0, 13);
            System.setProperty("app.demo.password", password);
            System.out.println("\n>>> FinTrack 360 (demo local) — usuario: analista · contraseña temporal: " + password + "\n");
        }
        SpringApplication.run(FinTrackApplication.class, args);
    }
}
