package cl.aduana.libertadores.controller;

import cl.aduana.libertadores.model.RolInstitucion;
import cl.aduana.libertadores.security.RequireRole;
import cl.aduana.libertadores.service.AduanaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/aduana")
@RequireRole({RolInstitucion.ADUANA})
public class AduanaController {

    private final AduanaService aduanaService;

    public AduanaController(AduanaService aduanaService) {
        this.aduanaService = aduanaService;
    }

    @PostMapping("/vehiculos/sat")
    public ResponseEntity<?> emitirSat(@RequestBody Map<String, String> body) throws Exception {
        Map<String, Object> result = aduanaService.consultarPatenteYEmitirSat(
                body.get("patente"),
                body.get("paisOrigen"),
                body.get("tipoVehiculo")
        );
        if (result.containsKey("alerta")) {
            return ResponseEntity.status(409).body(result);
        }
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
