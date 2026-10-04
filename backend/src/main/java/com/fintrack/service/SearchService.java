package com.fintrack.service;

import com.fintrack.dto.SearchResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Buscador global. Solo consulta la base de datos ficticia de la aplicación. */
@Service
public class SearchService {
    private static final int LIMIT = 6;
    private final JdbcTemplate jdbc;

    public SearchService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SearchResult> search(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        List<SearchResult> out = new ArrayList<>();
        if (q.length() < 2) {
            return out;
        }
        String like = "%" + q.replace("%", "").replace("_", "") + "%";

        add(out, "PERSONA", "/perfil", """
                select id as person_id, full_name as label, 'Documento ficticio ' || document as detail
                from persons where lower(full_name) like ? or document like ?""", like, like);
        add(out, "BANCO", "/bancos", """
                select null::bigint as person_id, name as label, 'Banco ficticio' as detail
                from banks where lower(name) like ?""", like);
        // Las cuentas solo se buscan por banco, tipo o últimos 4 dígitos; nunca se devuelve el número completo.
        add(out, "CUENTA", "/cuentas", """
                select a.person_id, a.type || ' ****' || right(a.number, 4) as label,
                       b.name || ' · ' || p.full_name as detail
                from accounts a join banks b on b.id = a.bank_id join persons p on p.id = a.person_id
                where lower(b.name) like ? or lower(a.type) like ? or right(a.number, 4) = ?""", like, like, q);
        add(out, "CRÉDITO", "/creditos", """
                select c.person_id, c.product as label, b.name || ' · ' || c.status || ' · ' || p.full_name as detail
                from credits c join banks b on b.id = c.bank_id join persons p on p.id = c.person_id
                where lower(c.product) like ? or lower(b.name) like ? or lower(c.status) like ?""", like, like, like);
        add(out, "PROCESO", "/judicial", """
                select person_id, process_number as label, process_type || ' · ' || city || ' · ' || status as detail
                from judicial_processes
                where lower(process_number) like ? or lower(court) like ? or lower(city) like ?
                   or lower(plaintiff) like ? or lower(defendant) like ? or lower(process_type) like ?""",
                like, like, like, like, like, like);
        add(out, "DEMANDA", "/judicial", """
                select person_id, lawsuit_number as label, claim_type || ' · ' || plaintiff as detail
                from lawsuits
                where lower(lawsuit_number) like ? or lower(claim_type) like ? or lower(plaintiff) like ?""",
                like, like, like);
        add(out, "ALERTA", "/alertas", """
                select person_id, title as label, severity || ' · ' || description as detail
                from alerts where lower(title) like ? or lower(description) like ?""", like, like);
        add(out, "FUENTE", "/debida-diligencia", """
                select null::bigint as person_id, name as label, 'Fuente simulada · ' || category as detail
                from sources where lower(name) like ? or lower(category) like ?""", like, like);
        return out;
    }

    private void add(List<SearchResult> out, String type, String route, String sql, Object... args) {
        out.addAll(jdbc.query(sql + " limit " + LIMIT, (rs, i) -> {
            long personId = rs.getLong("person_id");
            return new SearchResult(type, rs.getString("label"), rs.getString("detail"),
                    rs.wasNull() ? null : personId, route);
        }, args));
    }
}
