package cl.aduana.libertadores.repository;

import cl.aduana.libertadores.model.Pasajero;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public class PasajeroRepository {

    private final DataSource dataSource;

    public PasajeroRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long insert(Pasajero p) throws SQLException {
        String sql = """
            INSERT INTO pasajeros (documento_enc, tipo_documento, nombres_enc, apellidos_enc,
                fecha_nacimiento, nacionalidad, menor_edad, tutor_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getDocumentoEncriptado());
            ps.setString(2, p.getTipoDocumento());
            ps.setString(3, p.getNombresEncriptado());
            ps.setString(4, p.getApellidosEncriptado());
            ps.setDate(5, Date.valueOf(p.getFechaNacimiento()));
            ps.setString(6, p.getNacionalidad());
            ps.setBoolean(7, p.isMenorEdad());
            if (p.getTutorId() != null) {
                ps.setLong(8, p.getTutorId());
            } else {
                ps.setNull(8, Types.BIGINT);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo insertar pasajero");
    }

    public Optional<Pasajero> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM pasajeros WHERE id = ?";
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

    private Pasajero mapRow(ResultSet rs) throws SQLException {
        Pasajero p = new Pasajero();
        p.setId(rs.getLong("id"));
        p.setDocumentoEncriptado(rs.getString("documento_enc"));
        p.setTipoDocumento(rs.getString("tipo_documento"));
        p.setNombresEncriptado(rs.getString("nombres_enc"));
        p.setApellidosEncriptado(rs.getString("apellidos_enc"));
        Date fn = rs.getDate("fecha_nacimiento");
        if (fn != null) {
            p.setFechaNacimiento(fn.toLocalDate());
        }
        p.setNacionalidad(rs.getString("nacionalidad"));
        p.setMenorEdad(rs.getBoolean("menor_edad"));
        long tutor = rs.getLong("tutor_id");
        if (!rs.wasNull()) {
            p.setTutorId(tutor);
        }
        return p;
    }
}
