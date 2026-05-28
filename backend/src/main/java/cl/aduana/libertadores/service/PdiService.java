package cl.aduana.libertadores.service;

import cl.aduana.libertadores.model.Pasajero;
import cl.aduana.libertadores.model.PermisoMenor;
import cl.aduana.libertadores.repository.PasajeroRepository;
import cl.aduana.libertadores.repository.PermisoMenorRepository;
import cl.aduana.libertadores.util.AesEncryptionUtil;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Service
public class PdiService {

    private final PasajeroRepository pasajeroRepository;
    private final PermisoMenorRepository permisoMenorRepository;
    private final AesEncryptionUtil aes;

    public PdiService(PasajeroRepository pasajeroRepository,
                      PermisoMenorRepository permisoMenorRepository,
                      AesEncryptionUtil aes) {
        this.pasajeroRepository = pasajeroRepository;
        this.permisoMenorRepository = permisoMenorRepository;
        this.aes = aes;
    }

    public Map<String, Object> registrarPasajero(String documento, String tipoDoc, String nombres,
                                                  String apellidos, LocalDate fechaNacimiento,
                                                  String nacionalidad, Long tutorId) throws SQLException {
        boolean menor = Period.between(fechaNacimiento, LocalDate.now()).getYears() < 18;

        Pasajero p = new Pasajero();
        p.setDocumentoEncriptado(aes.encrypt(documento));
        p.setTipoDocumento(tipoDoc);
        p.setNombresEncriptado(aes.encrypt(nombres));
        p.setApellidosEncriptado(aes.encrypt(apellidos));
        p.setFechaNacimiento(fechaNacimiento);
        p.setNacionalidad(nacionalidad);
        p.setMenorEdad(menor);
        p.setTutorId(tutorId);

        Long id = pasajeroRepository.insert(p);

        Map<String, Object> result = new HashMap<>();
        result.put("pasajeroId", id);
        result.put("menorEdad", menor);
        result.put("requierePermiso", menor);
        if (menor) {
            result.put("mensaje", "Menor detectado: debe registrar permiso notarial o judicial antes de autorizar salida.");
        }
        return result;
    }

    public Map<String, Object> registrarPermisoMenor(Long pasajeroMenorId, String tipoPermiso,
                                                      String numeroDocumento, LocalDate fechaEmision) throws SQLException {
        PermisoMenor permiso = new PermisoMenor();
        permiso.setPasajeroMenorId(pasajeroMenorId);
        permiso.setTipoPermiso(tipoPermiso);
        permiso.setNumeroDocumento(numeroDocumento);
        permiso.setFechaEmision(fechaEmision);
        permiso.setValidado(false);

        Long permisoId = permisoMenorRepository.insert(permiso);

        Map<String, Object> result = new HashMap<>();
        result.put("permisoId", permisoId);
        result.put("estado", "PENDIENTE_VALIDACION");
        return result;
    }

    public Map<String, Object> validarPermisoYAutorizarSalida(Long pasajeroMenorId, Long empleadoId) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        if (!permisoMenorRepository.existePermisoValidado(pasajeroMenorId)) {
            var permisoOpt = permisoMenorRepository.findByPasajeroMenorId(pasajeroMenorId);
            if (permisoOpt.isEmpty()) {
                result.put("autorizado", false);
                result.put("error", "No existe permiso notarial/judicial registrado para el menor.");
                return result;
            }
            permisoMenorRepository.marcarValidado(permisoOpt.get().getId(), empleadoId);
        }
        result.put("autorizado", true);
        result.put("mensaje", "Salida del país autorizada para el menor (PDI).");
        return result;
    }

    public boolean puedeAutorizarSalidaMenor(Long pasajeroMenorId) throws SQLException {
        return permisoMenorRepository.existePermisoValidado(pasajeroMenorId);
    }
}
