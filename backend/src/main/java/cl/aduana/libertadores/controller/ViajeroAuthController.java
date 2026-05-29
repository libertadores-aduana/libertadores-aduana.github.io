package cl.aduana.libertadores.controller;

import cl.aduana.libertadores.service.ViajeroAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth/viajero")
public class ViajeroAuthController {

    private final ViajeroAuthService viajeroAuthService;

    public ViajeroAuthController(ViajeroAuthService viajeroAuthService) {
        this.viajeroAuthService = viajeroAuthService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = viajeroAuthService.registrar(
                    body.get("email"),
                    body.get("password"),
                    body.get("nombreCompleto"),
                    body.get("rut"),
                    body.get("telefono"),
                    body.get("nacionalidad")
            );
            if (result.containsKey("error")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            Optional<Map<String, Object>> result = viajeroAuthService.login(
                    body.get("email"),
                    body.get("password")
            );
            if (result.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
            }
            Map<String, Object> data = result.get();
            if (data.containsKey("error")) {
                return ResponseEntity.status(403).body(data);
            }
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/verificar")
    public ResponseEntity<?> verificar(@RequestParam String token) {
        try {
            Map<String, Object> result = viajeroAuthService.verificarEmail(token);
            if (result.containsKey("error")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reenviar-verificacion")
    public ResponseEntity<?> reenviar(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(viajeroAuthService.reenviarVerificacion(body.get("email")));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
