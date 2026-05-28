package cl.aduana.libertadores.controller;

import cl.aduana.libertadores.security.RbacInterceptor;
import cl.aduana.libertadores.security.SessionContext;
import cl.aduana.libertadores.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            Optional<Map<String, Object>> result = authService.login(
                    body.get("email"),
                    body.get("password")
            );
            return result.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas")));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        SessionContext session = (SessionContext) request.getAttribute(RbacInterceptor.SESSION_ATTR);
        if (session == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debe iniciar sesión"));
        }
        try {
            Map<String, Object> result = authService.cambiarPassword(
                    session.getEmpleadoId(),
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

    @PostMapping("/recuperar")
    public ResponseEntity<?> recuperar(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.solicitarRecuperacion(body.get("email")));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/restablecer")
    public ResponseEntity<?> restablecer(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = authService.restablecerPassword(
                    body.get("token"),
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
}
