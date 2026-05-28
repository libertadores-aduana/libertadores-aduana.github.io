package cl.aduana.libertadores.repository;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class EstadisticasRepository {

    private final DataSource dataSource;

    public EstadisticasRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Long> resumenGeneral() throws SQLException {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("totalPasajeros", count("SELECT COUNT(*) FROM pasajeros"));
        stats.put("totalMenores", count("SELECT COUNT(*) FROM pasajeros WHERE menor_edad = true"));
        stats.put("totalVehiculosSat", count("SELECT COUNT(*) FROM vehiculos_sat"));
        stats.put("alertasRobo", count("SELECT COUNT(*) FROM vehiculos_sat WHERE encargo_robo = true"));
        stats.put("declaracionesSag", count("SELECT COUNT(*) FROM declaraciones_juradas"));
        stats.put("permisosMenorValidados", count("SELECT COUNT(*) FROM permisos_menor WHERE validado = true"));
        return stats;
    }

    private long count(String sql) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }
}
