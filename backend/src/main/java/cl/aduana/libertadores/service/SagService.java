package cl.aduana.libertadores.service;

import cl.aduana.libertadores.model.DeclaracionJurada;
import cl.aduana.libertadores.model.Pasajero;
import cl.aduana.libertadores.repository.DeclaracionJuradaRepository;
import cl.aduana.libertadores.repository.PasajeroRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SagService {

    private final DeclaracionJuradaRepository declaracionRepository;
    private final PasajeroRepository pasajeroRepository;

    public SagService(DeclaracionJuradaRepository declaracionRepository,
                      PasajeroRepository pasajeroRepository) {
        this.declaracionRepository = declaracionRepository;
        this.pasajeroRepository = pasajeroRepository;
    }

    public Map<String, Object> registrarDeclaracion(Long pasajeroId, Long representanteId,
                                                     boolean productosAgro, boolean animales,
                                                     String detalle, Long empleadoSagId) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        Optional<Pasajero> pasajeroOpt = pasajeroRepository.findById(pasajeroId);
        if (pasajeroOpt.isEmpty()) {
            result.put("error", "Pasajero no encontrado");
            return result;
        }

        Pasajero pasajero = pasajeroOpt.get();
        int edad = Period.between(pasajero.getFechaNacimiento(), LocalDate.now()).getYears();

        if (edad < 18) {
            if (representanteId == null) {
                result.put("error", "Menor de 18 años: la declaración jurada debe ser completada por tutor o representante legal.");
                return result;
            }
        } else if (representanteId != null) {
            result.put("advertencia", "Pasajero mayor de edad: la declaración debe ser personal.");
        }

        DeclaracionJurada d = new DeclaracionJurada();
        d.setPasajeroId(pasajeroId);
        d.setRepresentanteId(representanteId);
        d.setProductosAgropecuarios(productosAgro);
        d.setAnimalesMascotas(animales);
        d.setDetalle(detalle);
        d.setFechaDeclaracion(LocalDateTime.now());
        d.setEmpleadoSagId(empleadoSagId);

        Long id = declaracionRepository.insert(d);

        result.put("declaracionId", id);
        result.put("registrada", true);
        if (productosAgro || animales) {
            result.put("fiscalizacion", "REQUIERE_INSPECCION_SAG");
        } else {
            result.put("fiscalizacion", "SIN_NOVEDAD");
        }
        return result;
    }
}
