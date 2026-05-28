package cl.aduana.libertadores.repository;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class TokenRecuperacionRepository {

    private final DataSource dataSource;

    public TokenRecuperacionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(Long empleadoId, String tokenHash, LocalDateTime expiraEn) throws SQLException {
        String sql = "INSERT INTO tokens_recuperacion (empleado_id, token_hash, expira_en) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, empleadoId);
            ps.setString(2, tokenHash);
            ps.setTimestamp(3, Timestamp.valueOf(expiraEn));
            ps.executeUpdate();
        }
    }

    public Optional<Long> findEmpleadoIdByTokenHash(String tokenHash) throws SQLException {
        String sql = """
            SELECT empleado_id FROM tokens_recuperacion
            WHERE token_hash = ? AND usado = false AND expira_en > NOW()
            ORDER BY id DESC LIMIT 1
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong("empleado_id"));
                }
            }
        }
        return Optional.empty();
    }

    public void marcarUsado(String tokenHash) throws SQLException {
        String sql = "UPDATE tokens_recuperacion SET usado = true WHERE token_hash = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            ps.executeUpdate();
        }
    }
}
