package cl.aduana.libertadores.controller;

import cl.aduana.libertadores.service.DocumentoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Pre-registro en línea para viajeros (como digitación en aduana.cl del caso EFT).
 * No requiere cuenta de funcionario.
 */
@RestController
@RequestMapping("/api/public")
public class PublicPreRegistroController {

    private final DocumentoService documentoService;

    public PublicPreRegistroController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @PostMapping("/preregistro")
    public ResponseEntity<?> crearPreRegistro(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(documentoService.crearPreRegistro(
                    body.get("tipoTramite"),
                    body.get("email"),
                    body.get("referencia")
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/seguimiento/{codigo}")
    public ResponseEntity<?> seguimiento(@PathVariable String codigo) {
        try {
            Map<String, Object> result = documentoService.consultarPreRegistro(codigo);
            if (result.containsKey("error")) {
                return ResponseEntity.notFound().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirDocumentoPublico(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam("codigoPreRegistro") String codigoPreRegistro) {
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
}
