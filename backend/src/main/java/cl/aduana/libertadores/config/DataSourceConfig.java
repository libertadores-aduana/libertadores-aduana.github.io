package cl.aduana.libertadores.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Conexión a Neon: pegue en Render la connection string completa (postgresql://...)
 * o JDBC con user/password en la URL.
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties props = new DataSourceProperties();
        props.setDriverClassName("org.postgresql.Driver");

        String databaseUrl = env("DATABASE_URL");
        String dbUser = env("DB_USER");
        String dbPassword = env("DB_PASSWORD");

        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            NeonParts parts = parseNeonUrl(databaseUrl);
            props.setUrl(parts.jdbcUrl());
            props.setUsername(parts.username());
            props.setPassword(parts.password());
            log.info("BD configurada desde connection string Neon (host en URL JDBC)");
            return props;
        }

        if (databaseUrl != null && !databaseUrl.isBlank()) {
            props.setUrl(databaseUrl);
            if (dbUser != null && !dbUser.isBlank()) {
                props.setUsername(dbUser);
            }
            if (dbPassword != null) {
                props.setPassword(dbPassword);
            }
            log.info("BD configurada desde DATABASE_URL JDBC + DB_USER/DB_PASSWORD");
            return props;
        }

        throw new IllegalStateException("Configure DATABASE_URL en Render (connection string de Neon)");
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        DataSource ds = properties.initializeDataSourceBuilder().build();
        if (ds instanceof HikariDataSource hikari) {
            hikari.setMaximumPoolSize(5);
        }
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
            if (!hostPart.contains("sslmode=")) {
                jdbcUrl += hostPart.contains("?") ? "&sslmode=require" : "?sslmode=require";
            }

            return new NeonParts(jdbcUrl, user, pass);
        } catch (Exception e) {
            throw new IllegalStateException("DATABASE_URL inválida: " + e.getMessage(), e);
        }
    }

    private record NeonParts(String jdbcUrl, String username, String password) {}
}
