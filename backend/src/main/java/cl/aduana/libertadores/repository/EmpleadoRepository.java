package cl.aduana.libertadores.repository;

import cl.aduana.libertadores.model.Empleado;
import cl.aduana.libertadores.model.RolInstitucion;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class EmpleadoRepository {

    private final DataSource dataSource;

    public EmpleadoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<Empleado> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, rut, nombre_completo, email, password_hash, rol, activo FROM empleados WHERE email = ? AND activo = true";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Empleado> findById(Long id) throws SQLException {
        String sql = "SELECT id, rut, nombre_completo, email, password_hash, rol, activo FROM empleados WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void updatePassword(Long empleadoId, String passwordHash) throws SQLException {
        String sql = "UPDATE empleados SET password_hash = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, empleadoId);
            ps.executeUpdate();
        }
    }

    private Empleado mapRow(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
        e.setId(rs.getLong("id"));
        e.setRut(rs.getString("rut"));
        e.setNombreCompleto(rs.getString("nombre_completo"));
        e.setEmail(rs.getString("email"));
        e.setPasswordHash(rs.getString("password_hash"));
        e.setRol(RolInstitucion.valueOf(rs.getString("rol")));
        e.setActivo(rs.getBoolean("activo"));
        return e;
    }
}
