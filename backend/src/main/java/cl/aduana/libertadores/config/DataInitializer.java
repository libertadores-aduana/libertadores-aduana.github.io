package cl.aduana.libertadores.config;

import cl.aduana.libertadores.util.PasswordUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Crea usuarios de demostración si la BD está vacía (solo desarrollo).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM empleados", Integer.class);
            if (count != null && count > 0) {
                return;
            }
            insert("11.111.111-1", "Funcionario PDI", "pdi@libertadores.cl", "Pdi123!", "PDI");
            insert("22.222.222-2", "Funcionario Aduana", "aduana@libertadores.cl", "Aduana123!", "ADUANA");
            insert("33.333.333-3", "Funcionario SAG", "sag@libertadores.cl", "Sag123!", "SAG");
            insert("99.999.999-9", "Administrador", "admin@libertadores.cl", "Admin123!", "ADMIN");
        } catch (Exception ignored) {
            // Tablas aún no creadas: ejecutar database/schema.sql primero
        }
    }

    private void insert(String rut, String nombre, String email, String password, String rol) {
        jdbcTemplate.update(
                "INSERT INTO empleados (rut, nombre_completo, email, password_hash, rol, activo) VALUES (?, ?, ?, ?, ?, true)",
                rut, nombre, email, PasswordUtil.hash(password), rol
        );
    }
}
