package cl.aduana.libertadores.repository;

import cl.aduana.libertadores.model.DeclaracionJurada;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;

@Repository
public class DeclaracionJuradaRepository {

    private final DataSource dataSource;

    public DeclaracionJuradaRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long insert(DeclaracionJurada d) throws SQLException {
        String sql = """
            INSERT INTO declaraciones_juradas (pasajero_id, representante_id, productos_agro, animales_mascotas,
                detalle, fecha_declaracion, empleado_sag_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, d.getPasajeroId());
            if (d.getRepresentanteId() != null) {
                ps.setLong(2, d.getRepresentanteId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setBoolean(3, d.isProductosAgropecuarios());
            ps.setBoolean(4, d.isAnimalesMascotas());
            ps.setString(5, d.getDetalle());
            ps.setTimestamp(6, Timestamp.valueOf(d.getFechaDeclaracion()));
            if (d.getEmpleadoSagId() != null) {
                ps.setLong(7, d.getEmpleadoSagId());
            } else {
                ps.setNull(7, Types.BIGINT);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo registrar declaración jurada");
    }

    public boolean existeDeclaracionParaPasajero(Long pasajeroId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM declaraciones_juradas WHERE pasajero_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pasajeroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
