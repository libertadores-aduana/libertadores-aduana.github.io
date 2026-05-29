package cl.aduana.libertadores.repository;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class TokenVerificacionEmailRepository {

    private final DataSource dataSource;

    public TokenVerificacionEmailRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(Long usuarioId, String tokenHash, LocalDateTime expiraEn) throws SQLException {
        String sql = """
            INSERT INTO tokens_verificacion_email (usuario_id, token_hash, expira_en)
            VALUES (?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            ps.setString(2, tokenHash);
            ps.setTimestamp(3, Timestamp.valueOf(expiraEn));
            ps.executeUpdate();
        }
    }

    public Optional<Long> findUsuarioIdByTokenHash(String tokenHash) throws SQLException {
        String sql = """
            SELECT usuario_id FROM tokens_verificacion_email
            WHERE token_hash = ? AND usado = FALSE AND expira_en > NOW()
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong("usuario_id"));
                }
            }
        }
        return Optional.empty();
    }

    public void marcarUsado(String tokenHash) throws SQLException {
        String sql = "UPDATE tokens_verificacion_email SET usado = TRUE WHERE token_hash = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            ps.executeUpdate();
        }
    }
}
