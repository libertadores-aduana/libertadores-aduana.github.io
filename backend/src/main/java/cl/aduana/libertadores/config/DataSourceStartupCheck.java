package cl.aduana.libertadores.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DataSourceStartupCheck {

    private static final Logger log = LoggerFactory.getLogger(DataSourceStartupCheck.class);

    private final DataSource dataSource;

    public DataSourceStartupCheck(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verifyConnection() {
        try (Connection conn = dataSource.getConnection()) {
            log.info("Conexión a PostgreSQL OK");
        } catch (Exception e) {
            log.error("Conexión a PostgreSQL FALLÓ: {}", e.getMessage());
        }
    }
}
