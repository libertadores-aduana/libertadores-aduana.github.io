package cl.aduana.libertadores.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Acepta DATABASE_URL en formato JDBC o el connection string de Neon (postgresql://...).
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(
            @Value("${DATABASE_URL:}") String databaseUrl,
            @Value("${DB_USER:}") String dbUser,
            @Value("${DB_PASSWORD:}") String dbPassword) {

        String jdbcUrl = databaseUrl;
        String username = dbUser;
        String password = dbPassword;

        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            NeonConnection parsed = parseNeonUrl(databaseUrl);
            jdbcUrl = parsed.jdbcUrl();
            username = parsed.username();
            password = parsed.password();
        }

        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalStateException("DATABASE_URL no está configurada en Render");
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }

    private NeonConnection parseNeonUrl(String url) {
        try {
            String withoutScheme = url.substring("postgresql://".length());
            int at = withoutScheme.lastIndexOf('@');
            if (at < 0) {
                throw new IllegalArgumentException("URL sin credenciales");
            }
            String userInfo = withoutScheme.substring(0, at);
            String hostPart = withoutScheme.substring(at + 1);

            int colon = userInfo.indexOf(':');
            String user = URLDecoder.decode(userInfo.substring(0, colon), StandardCharsets.UTF_8);
            String pass = URLDecoder.decode(userInfo.substring(colon + 1), StandardCharsets.UTF_8);

            String jdbcUrl = "jdbc:postgresql://" + hostPart;
            if (!hostPart.contains("?")) {
                jdbcUrl += "?sslmode=require";
            }

            return new NeonConnection(jdbcUrl, user, pass);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer DATABASE_URL de Neon: " + e.getMessage(), e);
        }
    }

    private record NeonConnection(String jdbcUrl, String username, String password) {}
}
