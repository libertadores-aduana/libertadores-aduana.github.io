package cl.aduana.libertadores.repository;

import cl.aduana.libertadores.model.VehiculoSat;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class VehiculoSatRepository {

    private final DataSource dataSource;

    public VehiculoSatRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long insert(VehiculoSat v) throws SQLException {
        String sql = """
            INSERT INTO vehiculos_sat (patente_enc, pais_origen, tipo_vehiculo, diplomatico,
                fecha_ingreso, fecha_max_salida, encargo_robo, estado_sat)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getPatenteEncriptada());
            ps.setString(2, v.getPaisOrigen());
            ps.setString(3, v.getTipoVehiculo());
            ps.setBoolean(4, v.isDiplomatico());
            ps.setDate(5, Date.valueOf(v.getFechaIngreso()));
            ps.setDate(6, Date.valueOf(v.getFechaMaxSalida()));
            ps.setBoolean(7, v.isEncargoRobo());
            ps.setString(8, v.getEstadoSat());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo registrar vehículo SAT");
    }

    public Optional<VehiculoSat> findByPatenteEncriptada(String patenteEnc) throws SQLException {
        String sql = "SELECT * FROM vehiculos_sat WHERE patente_enc = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patenteEnc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean tieneEncargoRobo(String patenteEnc) throws SQLException {
        String sql = "SELECT COUNT(*) FROM encargos_robo WHERE patente_enc = ? AND vigente = true";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patenteEnc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private VehiculoSat mapRow(ResultSet rs) throws SQLException {
        VehiculoSat v = new VehiculoSat();
        v.setId(rs.getLong("id"));
        v.setPatenteEncriptada(rs.getString("patente_enc"));
        v.setPaisOrigen(rs.getString("pais_origen"));
        v.setTipoVehiculo(rs.getString("tipo_vehiculo"));
        v.setDiplomatico(rs.getBoolean("diplomatico"));
        v.setFechaIngreso(rs.getDate("fecha_ingreso").toLocalDate());
        v.setFechaMaxSalida(rs.getDate("fecha_max_salida").toLocalDate());
        v.setEncargoRobo(rs.getBoolean("encargo_robo"));
        v.setEstadoSat(rs.getString("estado_sat"));
        return v;
    }
}
