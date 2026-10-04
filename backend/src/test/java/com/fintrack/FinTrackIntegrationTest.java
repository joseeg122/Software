package com.fintrack;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.service.DueDiligenceService;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Arranca la app completa contra un PostgreSQL embebido: Flyway + semilla + API. */
@SpringBootTest
@AutoConfigureMockMvc
class FinTrackIntegrationTest {
    private static EmbeddedPostgres postgres;
    // Credenciales aleatorias por ejecución: ningún secreto queda escrito en el código.
    private static final String PASSWORD = UUID.randomUUID().toString();

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbc;
    private String token;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) throws IOException {
        postgres = EmbeddedPostgres.start();
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "");
        registry.add("app.jwt.secret", () -> UUID.randomUUID() + "-" + UUID.randomUUID());
        registry.add("app.demo.password", () -> PASSWORD);
    }

    @AfterAll
    static void stop() throws IOException {
        postgres.close();
    }

    @BeforeEach
    void login() throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", "analista", "password", PASSWORD));
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        token = "Bearer " + mapper.readTree(response).get("token").asText();
    }

    private JsonNode get(String url) throws Exception {
        return mapper.readTree(mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url)
                        .header("Authorization", token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
    }

    private long count(String table) {
        return jdbc.queryForObject("select count(*) from " + table, Long.class);
    }

    @Test
    void sinTokenNoHayAcceso() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/persons"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"analista\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void laSemillaCreaLasCantidadesPedidas() {
        assertEquals(10, count("persons"));
        assertEquals(7, count("banks"));
        assertEquals(30, count("accounts"));
        assertEquals(200, count("transactions"));
        assertEquals(20, count("credits"));
        assertEquals(200, count("credit_payments"));
        assertEquals(15, count("judicial_processes"));
        assertEquals(10, count("lawsuits"));
        assertEquals(5, count("legal_measures"));
        assertEquals(30, count("alerts"));
        assertEquals(69, count("sources"));
        assertEquals(690, count("source_results"));
    }

    @Test
    void saldosEncadenadosEnLaBaseDeDatos() {
        assertEquals(0, jdbc.queryForObject("""
                select count(*) from transactions
                where balance_after <> balance_before + case when type = 'CREDITO' then amount else -amount end""",
                Long.class));
        // El saldo de cada cuenta es el saldo posterior de su último movimiento.
        assertEquals(0, jdbc.queryForObject("""
                select count(*) from accounts a
                where a.balance <> (select t.balance_after from transactions t where t.account_id = a.id
                                    order by t.tx_date desc, t.id desc limit 1)""", Long.class));
    }

    @Test
    void dashboardDeJuanPerezCoincideConElPrototipo() throws Exception {
        long id = jdbc.queryForObject("select id from persons where document = '1.000.000.001'", Long.class);
        JsonNode d = get("/api/persons/" + id + "/dashboard");
        assertEquals("Juan Pérez", d.get("fullName").asText());
        assertEquals(DueDiligenceService.STATUS_COMPLETE, d.get("overallStatus").asText());
        assertEquals(5, d.get("banksConnected").asInt());
        assertEquals(8, d.get("accounts").asInt());
        assertEquals(15_800_000, d.get("totalDebt").asLong());
        assertEquals(4, d.get("activeCredits").asInt());
        assertEquals(2, d.get("processes").asInt());
        assertEquals(3, d.get("findings").asInt());
        assertEquals(742, d.get("score").asInt());
        assertEquals(900, d.get("maxScore").asInt());
        assertEquals("2026-10-03T21:00:00", d.get("lastCheckedAt").asText());
    }

    @Test
    void lasCuentasNuncaSalenSinEnmascarar() throws Exception {
        String json = get("/api/persons/1/profile").toString();
        for (String number : jdbc.queryForList("select number from accounts union select number from credits", String.class)) {
            assertFalse(json.contains(number), "número completo expuesto");
        }
        JsonNode account = get("/api/persons/1/accounts").get(0);
        assertTrue(account.get("maskedNumber").asText().matches("\\*{4}\\d{4}"));
        assertNull(account.get("number"));
        assertFalse(get("/api/mock/banco-nova/accounts?document=1.000.000.001").toString().contains("\"number\""));
    }

    @Test
    void actualizarConsultaRegistraFallosSinConvertirlosEnSinHallazgos() throws Exception {
        long failures = 0;
        for (int i = 0; i < 3; i++) {
            JsonNode profile = mapper.readTree(mvc.perform(post("/api/persons/2/refresh").header("Authorization", token))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
            JsonNode dashboard = profile.get("dashboard");
            long unavailable = dashboard.get("unavailableSources").asLong();
            assertEquals(unavailable, profile.get("unavailableSources").size());
            for (JsonNode r : profile.get("unavailableSources")) {
                assertTrue(r.get("status").asText().matches("ERROR|NO_DISPONIBLE"));
            }
            // Con fuentes sin respuesta el estado general nunca puede decir "completada".
            assertEquals(unavailable == 0, DueDiligenceService.STATUS_COMPLETE.equals(dashboard.get("overallStatus").asText()));
            failures += unavailable;
        }
        assertTrue(failures > 0);
        assertEquals(690, count("source_results"));
        assertEquals(3, jdbc.queryForObject("select run_number from persons where id = 2", Integer.class));
    }

    @Test
    void comentariosReporteBuscadorEIa() throws Exception {
        String created = mvc.perform(post("/api/persons/3/comments").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"Revisar el proceso simulado.\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long commentId = mapper.readTree(created).get("id").asLong();
        mvc.perform(put("/api/comments/" + commentId).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"Comentario editado.\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.body").value("Comentario editado."));
        mvc.perform(post("/api/persons/3/comments").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"Documento 52.345.678 real\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/comments/" + commentId).header("Authorization", token))
                .andExpect(status().isNoContent());

        String report = mvc.perform(post("/api/persons/1/reports").header("Authorization", token))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode reportJson = mapper.readTree(report);
        assertTrue(reportJson.get("code").asText().startsWith("RPT-SIM-"));
        assertEquals("Juan Pérez", reportJson.get("snapshot").get("person").get("fullName").asText());
        assertTrue(reportJson.get("snapshot").get("disclaimer").asText().contains("DATOS COMPLETAMENTE SIMULADOS"));

        JsonNode results = get("/api/search?q=nova");
        assertTrue(results.size() > 0);
        assertEquals("BANCO", results.get(0).get("type").asText());
        assertEquals("PERSONA", get("/api/search?q=juan").get(0).get("type").asText());

        String answer = mvc.perform(post("/api/persons/1/ai/ask").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("question", "¿Qué obligaciones están en mora?"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode ai = mapper.readTree(answer);
        assertEquals(1, ai.get("systemData").size());
        assertTrue(ai.get("systemData").get(0).asText().contains("Banco Andino"));
        assertFalse(ai.get("interpretation").asText().isBlank());
    }
}
