package cl.aduana.libertadores.repository;

import cl.aduana.libertadores.model.UsuarioViajero;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class UsuarioViajeroRepository {

    private final DataSource dataSource;

    public UsuarioViajeroRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long insert(String email, String passwordHash, String nombreCompleto,
                       String rut, String telefono, String nacionalidad) throws SQLException {
        String sql = """
            INSERT INTO usuarios_viajeros (email, password_hash, nombre_completo, rut, telefono, nacionalidad)
            VALUES (?, ?, ?, ?, ?, ?) RETURNING id
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            ps.setString(3, nombreCompleto);
            ps.setString(4, rut);
            ps.setString(5, telefono);
            ps.setString(6, nacionalidad);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo crear usuario viajero");
    }

    public Optional<UsuarioViajero> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuarios_viajeros WHERE email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<UsuarioViajero> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM usuarios_viajeros WHERE id = ?";
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

    public void marcarEmailVerificado(Long id) throws SQLException {
        String sql = "UPDATE usuarios_viajeros SET email_verificado = TRUE WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void updatePerfil(Long id, String nombreCompleto, String rut, String telefono, String nacionalidad) throws SQLException {
        String sql = """
            UPDATE usuarios_viajeros
            SET nombre_completo = ?, rut = ?, telefono = ?, nacionalidad = ?
            WHERE id = ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreCompleto);
            ps.setString(2, rut);
            ps.setString(3, telefono);
            ps.setString(4, nacionalidad);
            ps.setLong(5, id);
            ps.executeUpdate();
        }
    }

    public void updatePassword(Long id, String passwordHash) throws SQLException {
        String sql = "UPDATE usuarios_viajeros SET password_hash = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    private UsuarioViajero mapRow(ResultSet rs) throws SQLException {
        UsuarioViajero u = new UsuarioViajero();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setRut(rs.getString("rut"));
        u.setTelefono(rs.getString("telefono"));
        u.setNacionalidad(rs.getString("nacionalidad"));
        u.setEmailVerificado(rs.getBoolean("email_verificado"));
        u.setActivo(rs.getBoolean("activo"));
        Timestamp ts = rs.getTimestamp("creado_en");
        if (ts != null) {
            u.setCreadoEn(ts.toLocalDateTime());
        }
        return u;
    }
}
