package cl.aduana.libertadores.service;

import cl.aduana.libertadores.model.DocumentoAdjunto;
import cl.aduana.libertadores.model.TipoDocumento;
import cl.aduana.libertadores.repository.DocumentoAdjuntoRepository;
import cl.aduana.libertadores.repository.PreRegistroRepository;
import cl.aduana.libertadores.util.AesEncryptionUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DocumentoService {

    private static final String CODIGO_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final DocumentoAdjuntoRepository documentoRepository;
    private final PreRegistroRepository preRegistroRepository;
    private final FileStorageService fileStorageService;
    private final AesEncryptionUtil aes;

    public DocumentoService(DocumentoAdjuntoRepository documentoRepository,
                            PreRegistroRepository preRegistroRepository,
                            FileStorageService fileStorageService,
                            AesEncryptionUtil aes) {
        this.documentoRepository = documentoRepository;
        this.preRegistroRepository = preRegistroRepository;
        this.fileStorageService = fileStorageService;
        this.aes = aes;
    }

    public Map<String, Object> subirDocumento(MultipartFile file, String tipoDocumento,
                                               Long pasajeroId, Long permisoMenorId, Long vehiculoSatId,
                                               String codigoPreRegistro, Long empleadoId) throws Exception {
        TipoDocumento.valueOf(tipoDocumento);

        Long preRegistroId = null;
        if (codigoPreRegistro != null && !codigoPreRegistro.isBlank()) {
            preRegistroId = preRegistroRepository.findIdByCodigo(codigoPreRegistro.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Código de pre-registro no válido"));
        }

        String ruta = fileStorageService.guardar(file);
        Long docId = documentoRepository.insert(
                preRegistroId, pasajeroId, permisoMenorId, vehiculoSatId, empleadoId,
                tipoDocumento, file.getOriginalFilename(), ruta,
                file.getContentType(), file.getSize()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("documentoId", docId);
        result.put("nombreArchivo", file.getOriginalFilename());
        result.put("tipoDocumento", tipoDocumento);
        result.put("mensaje", "Documento cargado correctamente. Presente este código en la ventanilla para agilizar el trámite.");
        return result;
    }

    public Map<String, Object> crearPreRegistro(String tipoTramite, String email, String referencia) throws SQLException {
        return crearPreRegistro(tipoTramite, email, referencia, null);
    }

    public Map<String, Object> crearPreRegistro(String tipoTramite, String email, String referencia, Long usuarioId) throws SQLException {
        String codigo = generarCodigo();
        String refEnc = referencia != null && !referencia.isBlank() ? aes.encrypt(referencia) : null;
        Long id = preRegistroRepository.insert(codigo, tipoTramite, email, refEnc, usuarioId);

        Map<String, Object> result = new HashMap<>();
        result.put("preRegistroId", id);
        result.put("codigoSeguimiento", codigo);
        result.put("mensaje", "Pre-registro creado. Guarde su código y suba los documentos antes de llegar al paso fronterizo.");
        return result;
    }

    public Map<String, Object> consultarPreRegistro(String codigo) throws SQLException {
        Optional<Map<String, Object>> detalle = preRegistroRepository.findDetalleByCodigo(codigo);
        if (detalle.isEmpty()) {
            return Map.of("error", "Código no encontrado");
        }
        Map<String, Object> result = new HashMap<>(detalle.get());
        Long id = (Long) result.get("id");
        List<DocumentoAdjunto> docs = documentoRepository.listByPreRegistroId(id);
        result.put("documentos", docs);
        result.remove("id");
        return result;
    }

    public byte[] descargarDocumento(Long documentoId) throws Exception {
        Optional<String> rutaOpt = documentoRepository.findRutaById(documentoId);
        if (rutaOpt.isEmpty()) {
            throw new IllegalArgumentException("Documento no encontrado");
        }
        Path path = fileStorageService.resolverRuta(rutaOpt.get());
        return Files.readAllBytes(path);
    }

    public List<DocumentoAdjunto> listarPorPasajero(Long pasajeroId) throws SQLException {
        return documentoRepository.listByPasajeroId(pasajeroId);
    }

    public List<Map<String, Object>> listarPreRegistrosPorUsuario(Long usuarioId) throws SQLException {
        return preRegistroRepository.findByUsuarioId(usuarioId);
    }

    private String generarCodigo() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CODIGO_CHARS.charAt(random.nextInt(CODIGO_CHARS.length())));
        }
        return sb.toString();
    }
}
