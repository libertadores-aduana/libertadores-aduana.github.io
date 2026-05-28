package cl.aduana.libertadores.controller;

import cl.aduana.libertadores.model.RolInstitucion;
import cl.aduana.libertadores.security.RequireRole;
import cl.aduana.libertadores.security.RbacInterceptor;
import cl.aduana.libertadores.security.SessionContext;
import cl.aduana.libertadores.service.PdiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/pdi")
@RequireRole({RolInstitucion.PDI})
public class PdiController {

    private final PdiService pdiService;

    public PdiController(PdiService pdiService) {
        this.pdiService = pdiService;
    }

    @PostMapping("/pasajeros")
    public ResponseEntity<?> registrarPasajero(@RequestBody Map<String, Object> body) throws Exception {
        Map<String, Object> result = pdiService.registrarPasajero(
                (String) body.get("documento"),
                (String) body.get("tipoDocumento"),
                (String) body.get("nombres"),
                (String) body.get("apellidos"),
                LocalDate.parse((String) body.get("fechaNacimiento")),
                (String) body.get("nacionalidad"),
                body.get("tutorId") != null ? Long.valueOf(body.get("tutorId").toString()) : null
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/menores/permiso")
    public ResponseEntity<?> registrarPermiso(@RequestBody Map<String, Object> body) throws Exception {
        return ResponseEntity.ok(pdiService.registrarPermisoMenor(
                Long.valueOf(body.get("pasajeroMenorId").toString()),
                (String) body.get("tipoPermiso"),
                (String) body.get("numeroDocumento"),
                LocalDate.parse((String) body.get("fechaEmision"))
        ));
    }

    @PostMapping("/menores/autorizar-salida")
    public ResponseEntity<?> autorizarSalida(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        SessionContext session = (SessionContext) request.getAttribute(RbacInterceptor.SESSION_ATTR);
        return ResponseEntity.ok(pdiService.validarPermisoYAutorizarSalida(
                Long.valueOf(body.get("pasajeroMenorId").toString()),
                session.getEmpleadoId()
        ));
    }
}
