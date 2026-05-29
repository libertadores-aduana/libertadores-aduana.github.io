package cl.aduana.libertadores.repository;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class PreRegistroRepository {

    private final DataSource dataSource;

    public PreRegistroRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long insert(String codigo, String tipoTramite, String email, String referenciaEnc, Long usuarioId) throws SQLException {
        String sql = """
            INSERT INTO pre_registros (codigo, tipo_tramite, email_contacto, referencia_enc, usuario_id)
            VALUES (?, ?, ?, ?, ?) RETURNING id
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setString(2, tipoTramite);
            ps.setString(3, email);
            ps.setString(4, referenciaEnc);
            if (usuarioId != null) {
                ps.setLong(5, usuarioId);
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo crear pre-registro");
    }

    public Optional<Long> findIdByCodigo(String codigo) throws SQLException {
        String sql = "SELECT id, tipo_tramite, email_contacto, creado_en FROM pre_registros WHERE codigo = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong("id"));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Map<String, Object>> findDetalleByCodigo(String codigo) throws SQLException {
        String sql = "SELECT id, codigo, tipo_tramite, email_contacto, creado_en FROM pre_registros WHERE codigo = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", rs.getLong("id"));
                    m.put("codigo", rs.getString("codigo"));
                    m.put("tipoTramite", rs.getString("tipo_tramite"));
                    m.put("emailContacto", rs.getString("email_contacto"));
                    Timestamp ts = rs.getTimestamp("creado_en");
                    if (ts != null) {
                        m.put("creadoEn", ts.toLocalDateTime().toString());
                    }
                    return Optional.of(m);
                }
            }
        }
        return Optional.empty();
    }

    public java.util.List<Map<String, Object>> findByUsuarioId(Long usuarioId) throws SQLException {
        String sql = """
            SELECT id, codigo, tipo_tramite, email_contacto, creado_en
            FROM pre_registros WHERE usuario_id = ? ORDER BY creado_en DESC
            """;
        java.util.List<Map<String, Object>> lista = new java.util.ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", rs.getLong("id"));
                    m.put("codigo", rs.getString("codigo"));
                    m.put("tipoTramite", rs.getString("tipo_tramite"));
                    m.put("emailContacto", rs.getString("email_contacto"));
                    Timestamp ts = rs.getTimestamp("creado_en");
                    if (ts != null) {
                        m.put("creadoEn", ts.toLocalDateTime().toString());
                    }
                    lista.add(m);
                }
            }
        }
        return lista;
    }
}
