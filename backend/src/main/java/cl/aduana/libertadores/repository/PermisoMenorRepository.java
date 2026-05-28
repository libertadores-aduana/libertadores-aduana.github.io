package cl.aduana.libertadores.repository;

import cl.aduana.libertadores.model.PermisoMenor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class PermisoMenorRepository {

    private final DataSource dataSource;

    public PermisoMenorRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long insert(PermisoMenor permiso) throws SQLException {
        String sql = """
            INSERT INTO permisos_menor (pasajero_menor_id, tipo_permiso, numero_documento, fecha_emision, validado, empleado_validador_id)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, permiso.getPasajeroMenorId());
            ps.setString(2, permiso.getTipoPermiso());
            ps.setString(3, permiso.getNumeroDocumento());
            ps.setDate(4, Date.valueOf(permiso.getFechaEmision()));
            ps.setBoolean(5, permiso.isValidado());
            if (permiso.getEmpleadoValidadorId() != null) {
                ps.setLong(6, permiso.getEmpleadoValidadorId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo insertar permiso de menor");
    }

    public boolean existePermisoValidado(Long pasajeroMenorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM permisos_menor WHERE pasajero_menor_id = ? AND validado = true";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pasajeroMenorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public Optional<PermisoMenor> findByPasajeroMenorId(Long pasajeroMenorId) throws SQLException {
        String sql = "SELECT * FROM permisos_menor WHERE pasajero_menor_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pasajeroMenorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PermisoMenor p = new PermisoMenor();
                    p.setId(rs.getLong("id"));
                    p.setPasajeroMenorId(rs.getLong("pasajero_menor_id"));
                    p.setTipoPermiso(rs.getString("tipo_permiso"));
                    p.setNumeroDocumento(rs.getString("numero_documento"));
                    p.setFechaEmision(rs.getDate("fecha_emision").toLocalDate());
                    p.setValidado(rs.getBoolean("validado"));
                    long emp = rs.getLong("empleado_validador_id");
                    if (!rs.wasNull()) {
                        p.setEmpleadoValidadorId(emp);
                    }
                    return Optional.of(p);
                }
            }
        }
        return Optional.empty();
    }

    public void marcarValidado(Long permisoId, Long empleadoId) throws SQLException {
        String sql = "UPDATE permisos_menor SET validado = true, empleado_validador_id = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, empleadoId);
            ps.setLong(2, permisoId);
            ps.executeUpdate();
        }
    }
}
