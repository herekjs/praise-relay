package kr.dbinc.praise;

import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class EntryRepository {

    private final JdbcTemplate jdbc;

    public EntryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> latest(int limit) {
        return jdbc.query(
                "SELECT id, name, message, created_at FROM entries ORDER BY id DESC LIMIT ?",
                (rs, i) -> row(rs.getLong("id"), rs.getString("name"),
                        rs.getString("message"), rs.getString("created_at")),
                limit);
    }

    public Map<String, Object> insert(String name, String message) {
        String createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS).toString();
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO entries (name, message, created_at) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, message);
            ps.setString(3, createdAt);
            return ps;
        }, keys);
        return row(keys.getKey().longValue(), name, message, createdAt);
    }

    private static Map<String, Object> row(long id, String name, String message, String createdAt) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("message", message);
        m.put("created_at", createdAt);
        return m;
    }
}
