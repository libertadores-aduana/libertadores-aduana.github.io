package cl.aduana.libertadores.controller;

import cl.aduana.libertadores.model.RolInstitucion;
import cl.aduana.libertadores.security.RequireRole;
import cl.aduana.libertadores.security.RbacInterceptor;
import cl.aduana.libertadores.security.SessionContext;
import cl.aduana.libertadores.service.SagService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sag")
@RequireRole({RolInstitucion.SAG})
public class SagController {

    private final SagService sagService;

    public SagController(SagService sagService) {
        this.sagService = sagService;
    }

    @PostMapping("/declaraciones")
    public ResponseEntity<?> registrarDeclaracion(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        SessionContext session = (SessionContext) request.getAttribute(RbacInterceptor.SESSION_ATTR);
        Map<String, Object> result = sagService.registrarDeclaracion(
                Long.valueOf(body.get("pasajeroId").toString()),
                body.get("representanteId") != null ? Long.valueOf(body.get("representanteId").toString()) : null,
                Boolean.TRUE.equals(body.get("productosAgro")),
                Boolean.TRUE.equals(body.get("animalesMascotas")),
                (String) body.get("detalle"),
                session.getEmpleadoId()
        );
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
