package cl.aduana.libertadores.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Conexión a Neon: pegue en Render DATABASE_URL con la connection string completa (postgresql://...).
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = env("DATABASE_URL");
        String dbUser = env("DB_USER");
        String dbPassword = env("DB_PASSWORD");

        String jdbcUrl;
        String username;
        String password;

        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            NeonParts parts = parseNeonUrl(databaseUrl);
            jdbcUrl = parts.jdbcUrl();
            username = parts.username();
            password = parts.password();
            log.info("BD: connection string Neon convertida a JDBC");
        } else if (databaseUrl != null && databaseUrl.startsWith("jdbc:postgresql://")) {
            jdbcUrl = databaseUrl;
            username = dbUser != null ? dbUser : "postgres";
            password = dbPassword != null ? dbPassword : "";
            log.info("BD: URL JDBC directa");
        } else if (databaseUrl != null && !databaseUrl.isBlank()) {
            jdbcUrl = "jdbc:postgresql://" + databaseUrl.replaceFirst("^jdbc:postgresql://", "");
            username = dbUser != null ? dbUser : "postgres";
            password = dbPassword != null ? dbPassword : "";
        } else {
            jdbcUrl = "jdbc:postgresql://localhost:5432/libertadores";
            username = dbUser != null ? dbUser : "postgres";
            password = dbPassword != null ? dbPassword : "postgres";
            log.warn("BD: usando localhost (solo desarrollo local)");
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(5);
        return ds;
    }

    private static String env(String key) {
        String value = System.getenv(key);
        return value != null && !value.isBlank() ? value.trim() : null;
    }

    private NeonParts parseNeonUrl(String url) {
        try {
            String withoutScheme = url.substring("postgresql://".length());
            int at = withoutScheme.lastIndexOf('@');
            if (at < 0) {
                throw new IllegalArgumentException("Falta @ en la URL");
            }
            String userInfo = withoutScheme.substring(0, at);
            String hostPart = withoutScheme.substring(at + 1);

            int colon = userInfo.indexOf(':');
            if (colon < 0) {
                throw new IllegalArgumentException("Falta usuario:contraseña");
            }

            String user = URLDecoder.decode(userInfo.substring(0, colon), StandardCharsets.UTF_8);
            String pass = URLDecoder.decode(userInfo.substring(colon + 1), StandardCharsets.UTF_8);
            String jdbcUrl = "jdbc:postgresql://" + hostPart;

            return new NeonParts(jdbcUrl, user, pass);
        } catch (Exception e) {
            throw new IllegalStateException("DATABASE_URL inválida: " + e.getMessage(), e);
        }
    }

    private record NeonParts(String jdbcUrl, String username, String password) {}
}
