package cl.aduana.libertadores.controller;

import cl.aduana.libertadores.model.RolInstitucion;
import cl.aduana.libertadores.repository.DocumentoAdjuntoRepository;
import cl.aduana.libertadores.security.RequireRole;
import cl.aduana.libertadores.security.RbacInterceptor;
import cl.aduana.libertadores.security.SessionContext;
import cl.aduana.libertadores.service.DocumentoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/documentos")
@RequireRole({RolInstitucion.PDI, RolInstitucion.ADUANA, RolInstitucion.SAG, RolInstitucion.ADMIN})
public class DocumentoController {

    private final DocumentoService documentoService;
    private final DocumentoAdjuntoRepository documentoAdjuntoRepository;

    public DocumentoController(DocumentoService documentoService,
                               DocumentoAdjuntoRepository documentoAdjuntoRepository) {
        this.documentoService = documentoService;
        this.documentoAdjuntoRepository = documentoAdjuntoRepository;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam(value = "pasajeroId", required = false) Long pasajeroId,
            @RequestParam(value = "permisoMenorId", required = false) Long permisoMenorId,
            @RequestParam(value = "vehiculoSatId", required = false) Long vehiculoSatId,
            @RequestParam(value = "codigoPreRegistro", required = false) String codigoPreRegistro,
            HttpServletRequest request) {
        try {
            SessionContext session = (SessionContext) request.getAttribute(RbacInterceptor.SESSION_ATTR);
            Map<String, Object> result = documentoService.subirDocumento(
                    archivo, tipoDocumento, pasajeroId, permisoMenorId, vehiculoSatId,
                    codigoPreRegistro, session.getEmpleadoId()
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) throws Exception {
        var meta = documentoAdjuntoRepository.findMetaById(id);
        if (meta.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        byte[] data = documentoService.descargarDocumento(id);
        String nombre = meta.get().getNombreArchivo();
        String mime = meta.get().getMimeType() != null ? meta.get().getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.parseMediaType(mime))
                .body(data);
    }

    @GetMapping("/pasajero/{pasajeroId}")
    public ResponseEntity<?> listarPorPasajero(@PathVariable Long pasajeroId) throws Exception {
        return ResponseEntity.ok(documentoService.listarPorPasajero(pasajeroId));
    }
}
