package cl.aduana.libertadores.controller;

import cl.aduana.libertadores.security.RbacInterceptor;
import cl.aduana.libertadores.security.SessionContext;
import cl.aduana.libertadores.service.DocumentoService;
import cl.aduana.libertadores.service.ViajeroAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/viajero")
public class ViajeroPerfilController {

    private final ViajeroAuthService viajeroAuthService;
    private final DocumentoService documentoService;

    public ViajeroPerfilController(ViajeroAuthService viajeroAuthService, DocumentoService documentoService) {
        this.viajeroAuthService = viajeroAuthService;
        this.documentoService = documentoService;
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(HttpServletRequest request) {
        SessionContext session = requireViajero(request);
        if (session == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debe iniciar sesión como viajero"));
        }
        try {
            return ResponseEntity.ok(viajeroAuthService.obtenerPerfil(session.getViajeroId()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarPerfil(@RequestBody Map<String, String> body, HttpServletRequest request) {
        SessionContext session = requireViajero(request);
        if (session == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debe iniciar sesión como viajero"));
        }
        try {
            Map<String, Object> result = viajeroAuthService.actualizarPerfil(session.getViajeroId(), body);
            if (result.containsKey("error")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        SessionContext session = requireViajero(request);
        if (session == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debe iniciar sesión como viajero"));
        }
        try {
            Map<String, Object> result = viajeroAuthService.cambiarPassword(
                    session.getViajeroId(),
                    body.get("passwordActual"),
                    body.get("passwordNueva")
            );
            if (result.containsKey("error")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/preregistros")
    public ResponseEntity<?> misPreRegistros(HttpServletRequest request) {
        SessionContext session = requireViajero(request);
        if (session == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debe iniciar sesión como viajero"));
        }
        try {
            return ResponseEntity.ok(documentoService.listarPreRegistrosPorUsuario(session.getViajeroId()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/preregistro")
    public ResponseEntity<?> crearPreRegistro(@RequestBody Map<String, String> body, HttpServletRequest request) {
        SessionContext session = requireViajero(request);
        if (session == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debe iniciar sesión como viajero"));
        }
        try {
            return ResponseEntity.ok(documentoService.crearPreRegistro(
                    body.get("tipoTramite"),
                    session.getEmail(),
                    body.get("referencia"),
                    session.getViajeroId()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirDocumento(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam("codigoPreRegistro") String codigoPreRegistro,
            HttpServletRequest request) {
        SessionContext session = requireViajero(request);
        if (session == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debe iniciar sesión como viajero"));
        }
        try {
            Map<String, Object> result = documentoService.subirDocumento(
                    archivo, tipoDocumento, null, null, null,
                    codigoPreRegistro, null
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private SessionContext requireViajero(HttpServletRequest request) {
        SessionContext session = (SessionContext) request.getAttribute(RbacInterceptor.SESSION_ATTR);
        if (session == null || !session.esViajero()) {
            return null;
        }
        return session;
    }
}
