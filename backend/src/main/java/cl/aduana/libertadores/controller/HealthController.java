package cl.aduana.libertadores.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public Map<String, String> health() {
        return Map.of("status", "UP", "sistema", "Los Libertadores");
    }

    @GetMapping("/db")
    public Map<String, Object> healthDb() {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            result.put("database", "UP");
            result.put("ok", true);
        } catch (Exception e) {
            result.put("database", "DOWN");
            result.put("ok", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
