package cl.aduana.libertadores.service;

import cl.aduana.libertadores.model.Empleado;
import cl.aduana.libertadores.repository.EmpleadoRepository;
import cl.aduana.libertadores.repository.TokenRecuperacionRepository;
import cl.aduana.libertadores.security.AuthTokenFilter;
import cl.aduana.libertadores.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final EmpleadoRepository empleadoRepository;
    private final TokenRecuperacionRepository tokenRecuperacionRepository;
    private final String frontendBaseUrl;

    public AuthService(EmpleadoRepository empleadoRepository,
                       TokenRecuperacionRepository tokenRecuperacionRepository,
                       @Value("${app.frontend.base-url:http://localhost:5500}") String frontendBaseUrl) {
        this.empleadoRepository = empleadoRepository;
        this.tokenRecuperacionRepository = tokenRecuperacionRepository;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public Optional<Map<String, Object>> login(String email, String password) throws SQLException {
        Optional<Empleado> empleadoOpt = empleadoRepository.findByEmail(email);
        if (empleadoOpt.isEmpty()) {
            return Optional.empty();
        }
        Empleado empleado = empleadoOpt.get();
        if (!PasswordUtil.verify(password, empleado.getPasswordHash())) {
            return Optional.empty();
        }
        String token = AuthTokenFilter.crearToken(empleado);
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("rol", empleado.getRol().name());
        response.put("nombre", empleado.getNombreCompleto());
        response.put("empleadoId", empleado.getId());
        return Optional.of(response);
    }

    public Map<String, Object> cambiarPassword(Long empleadoId, String passwordActual, String passwordNueva) throws SQLException {
        if (passwordNueva == null || passwordNueva.length() < 8) {
            return Map.of("error", "La nueva contraseña debe tener al menos 8 caracteres");
        }
        Optional<Empleado> empleadoOpt = empleadoRepository.findById(empleadoId);
        if (empleadoOpt.isEmpty()) {
            return Map.of("error", "Usuario no encontrado");
        }
        Empleado empleado = empleadoOpt.get();
        if (!PasswordUtil.verify(passwordActual, empleado.getPasswordHash())) {
            return Map.of("error", "Contraseña actual incorrecta");
        }
        empleadoRepository.updatePassword(empleadoId, PasswordUtil.hash(passwordNueva));
        return Map.of("mensaje", "Contraseña actualizada correctamente");
    }

    public Map<String, Object> solicitarRecuperacion(String email) throws SQLException {
        Optional<Empleado> empleadoOpt = empleadoRepository.findByEmail(email);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Si el correo está registrado, recibirá instrucciones para restablecer su contraseña.");

        if (empleadoOpt.isEmpty()) {
            return response;
        }

        String tokenPlano = generarTokenSeguro();
        String tokenHash = hashToken(tokenPlano);
        tokenRecuperacionRepository.insert(
                empleadoOpt.get().getId(),
                tokenHash,
                LocalDateTime.now().plusHours(1)
        );

        String enlace = frontendBaseUrl + "/restablecer.html?token=" + tokenPlano;
        response.put("enlaceRecuperacion", enlace);
        response.put("notaDesarrollo", "En producción este enlace se envía por correo institucional. No compartir públicamente.");
        return response;
    }

    public Map<String, Object> restablecerPassword(String token, String passwordNueva) throws SQLException {
        if (passwordNueva == null || passwordNueva.length() < 8) {
            return Map.of("error", "La contraseña debe tener al menos 8 caracteres");
        }
        String tokenHash = hashToken(token);
        Optional<Long> empleadoIdOpt = tokenRecuperacionRepository.findEmpleadoIdByTokenHash(tokenHash);
        if (empleadoIdOpt.isEmpty()) {
            return Map.of("error", "Enlace inválido o expirado");
        }
        empleadoRepository.updatePassword(empleadoIdOpt.get(), PasswordUtil.hash(passwordNueva));
        tokenRecuperacionRepository.marcarUsado(tokenHash);
        return Map.of("mensaje", "Contraseña restablecida. Ya puede iniciar sesión.");
    }

    private String generarTokenSeguro() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Error al procesar token", e);
        }
    }
}
