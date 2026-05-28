package cl.aduana.libertadores.service;

import cl.aduana.libertadores.model.VehiculoSat;
import cl.aduana.libertadores.repository.VehiculoSatRepository;
import cl.aduana.libertadores.util.AesEncryptionUtil;
import cl.aduana.libertadores.util.PatenteValidator;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AduanaService {

    private final VehiculoSatRepository vehiculoSatRepository;
    private final AesEncryptionUtil aes;

    public AduanaService(VehiculoSatRepository vehiculoSatRepository, AesEncryptionUtil aes) {
        this.vehiculoSatRepository = vehiculoSatRepository;
        this.aes = aes;
    }

    /**
     * Simula interoperabilidad Chile-Argentina consultando encargos y emitiendo formulario SAT.
     */
    public Map<String, Object> consultarPatenteYEmitirSat(String patente, String paisOrigen, String tipoVehiculo)
            throws SQLException {
        String normalizada = PatenteValidator.normalizar(patente);
        Map<String, Object> result = new HashMap<>();

        boolean chilena = PatenteValidator.esFormatoChileno(normalizada);
        boolean argentina = PatenteValidator.esFormatoArgentino(normalizada);

        if (!chilena && !argentina) {
            result.put("error", "Formato de patente inválido. Chile: ABCD12 | Argentina: AB123CD");
            return result;
        }

        String patenteEnc = aes.encrypt(normalizada);
        boolean encargo = vehiculoSatRepository.tieneEncargoRobo(patenteEnc);
        boolean diplomatico = PatenteValidator.esPlacaDiplomatica(normalizada);

        if (encargo) {
            result.put("alerta", "ALERTA INMEDIATA: Patente con encargo vigente por robo.");
            result.put("encargoRobo", true);
            result.put("autorizado", false);
            return result;
        }

        int diasPlazo = diplomatico ? 90 : 180;
        LocalDate fechaIngreso = LocalDate.now();
        LocalDate fechaMax = fechaIngreso.plusDays(diasPlazo);

        VehiculoSat v = new VehiculoSat();
        v.setPatenteEncriptada(patenteEnc);
        v.setPaisOrigen(paisOrigen != null ? paisOrigen : (chilena ? "CHILE" : "ARGENTINA"));
        v.setTipoVehiculo(tipoVehiculo != null ? tipoVehiculo : "PARTICULAR");
        v.setDiplomatico(diplomatico);
        v.setFechaIngreso(fechaIngreso);
        v.setFechaMaxSalida(fechaMax);
        v.setEncargoRobo(false);
        v.setEstadoSat("EMITIDO");

        Long satId = vehiculoSatRepository.insert(v);

        result.put("satId", satId);
        result.put("formulario", "Salida y Admisión Temporal de Vehículos (Binacional CL-AR)");
        result.put("patenteConsultada", normalizada);
        result.put("interoperabilidad", "Simulación consulta aduanas Chile y Argentina - OK");
        result.put("plazoDias", diasPlazo);
        result.put("fechaMaxSalida", fechaMax.toString());
        result.put("diplomatico", diplomatico);
        result.put("encargoRobo", false);
        result.put("autorizado", true);
        return result;
    }
}
