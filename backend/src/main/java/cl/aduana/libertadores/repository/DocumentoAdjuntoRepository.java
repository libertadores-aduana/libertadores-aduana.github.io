package cl.aduana.libertadores.repository;

import cl.aduana.libertadores.model.DocumentoAdjunto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DocumentoAdjuntoRepository {

    private final DataSource dataSource;

    public DocumentoAdjuntoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long insert(Long preRegistroId, Long pasajeroId, Long permisoMenorId, Long vehiculoSatId,
                       Long empleadoId, String tipoDocumento, String nombreArchivo,
                       String ruta, String mimeType, long tamano) throws SQLException {
        String sql = """
            INSERT INTO documentos_adjuntos (pre_registro_id, pasajero_id, permiso_menor_id, vehiculo_sat_id,
                empleado_subidor_id, tipo_documento, nombre_archivo, ruta_almacenamiento, mime_type, tamano_bytes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, preRegistroId);
            setNullableLong(ps, 2, pasajeroId);
            setNullableLong(ps, 3, permisoMenorId);
            setNullableLong(ps, 4, vehiculoSatId);
            setNullableLong(ps, 5, empleadoId);
            ps.setString(6, tipoDocumento);
            ps.setString(7, nombreArchivo);
            ps.setString(8, ruta);
            ps.setString(9, mimeType);
            ps.setLong(10, tamano);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo guardar documento");
    }

    public Optional<String> findRutaById(Long id) throws SQLException {
        String sql = "SELECT ruta_almacenamiento, nombre_archivo, mime_type FROM documentos_adjuntos WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("ruta_almacenamiento"));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<DocumentoAdjunto> findMetaById(Long id) throws SQLException {
        String sql = "SELECT id, pre_registro_id, pasajero_id, tipo_documento, nombre_archivo, mime_type, tamano_bytes, subido_en FROM documentos_adjuntos WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DocumentoAdjunto d = new DocumentoAdjunto();
                    d.setId(rs.getLong("id"));
                    long pr = rs.getLong("pre_registro_id");
                    if (!rs.wasNull()) d.setPreRegistroId(pr);
                    long pa = rs.getLong("pasajero_id");
                    if (!rs.wasNull()) d.setPasajeroId(pa);
                    d.setTipoDocumento(rs.getString("tipo_documento"));
                    d.setNombreArchivo(rs.getString("nombre_archivo"));
                    d.setMimeType(rs.getString("mime_type"));
                    d.setTamanoBytes(rs.getLong("tamano_bytes"));
                    Timestamp ts = rs.getTimestamp("subido_en");
                    if (ts != null) d.setSubidoEn(ts.toLocalDateTime());
                    return Optional.of(d);
                }
            }
        }
        return Optional.empty();
    }

    public List<DocumentoAdjunto> listByPreRegistroId(Long preRegistroId) throws SQLException {
        String sql = "SELECT id, tipo_documento, nombre_archivo, mime_type, tamano_bytes, subido_en FROM documentos_adjuntos WHERE pre_registro_id = ? ORDER BY subido_en";
        List<DocumentoAdjunto> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, preRegistroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DocumentoAdjunto d = new DocumentoAdjunto();
                    d.setId(rs.getLong("id"));
                    d.setTipoDocumento(rs.getString("tipo_documento"));
                    d.setNombreArchivo(rs.getString("nombre_archivo"));
                    d.setMimeType(rs.getString("mime_type"));
                    d.setTamanoBytes(rs.getLong("tamano_bytes"));
                    Timestamp ts = rs.getTimestamp("subido_en");
                    if (ts != null) d.setSubidoEn(ts.toLocalDateTime());
                    list.add(d);
                }
            }
        }
        return list;
    }

    public List<DocumentoAdjunto> listByPasajeroId(Long pasajeroId) throws SQLException {
        String sql = "SELECT id, tipo_documento, nombre_archivo, mime_type, tamano_bytes, subido_en FROM documentos_adjuntos WHERE pasajero_id = ? ORDER BY subido_en";
        List<DocumentoAdjunto> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pasajeroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DocumentoAdjunto d = new DocumentoAdjunto();
                    d.setId(rs.getLong("id"));
                    d.setTipoDocumento(rs.getString("tipo_documento"));
                    d.setNombreArchivo(rs.getString("nombre_archivo"));
                    d.setMimeType(rs.getString("mime_type"));
                    d.setTamanoBytes(rs.getLong("tamano_bytes"));
                    Timestamp ts = rs.getTimestamp("subido_en");
                    if (ts != null) d.setSubidoEn(ts.toLocalDateTime());
                    list.add(d);
                }
            }
        }
        return list;
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value != null) {
            ps.setLong(index, value);
        } else {
            ps.setNull(index, Types.BIGINT);
        }
    }
}
