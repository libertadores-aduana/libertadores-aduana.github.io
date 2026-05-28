package cl.aduana.libertadores.controller;

import cl.aduana.libertadores.model.RolInstitucion;
import cl.aduana.libertadores.security.RequireRole;
import cl.aduana.libertadores.service.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@RequireRole({RolInstitucion.PDI, RolInstitucion.ADUANA, RolInstitucion.SAG, RolInstitucion.ADMIN})
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Long>> resumen() throws Exception {
        return ResponseEntity.ok(reporteService.resumenJson());
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf() throws Exception {
        byte[] data = reporteService.generarPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-libertadores.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel() throws Exception {
        byte[] data = reporteService.generarExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-libertadores.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
