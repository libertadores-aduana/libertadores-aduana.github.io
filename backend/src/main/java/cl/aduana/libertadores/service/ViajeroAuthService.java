package cl.aduana.libertadores.service;

import cl.aduana.libertadores.model.UsuarioViajero;
import cl.aduana.libertadores.repository.TokenVerificacionEmailRepository;
import cl.aduana.libertadores.repository.UsuarioViajeroRepository;
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
import java.util.regex.Pattern;

@Service
public class ViajeroAuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UsuarioViajeroRepository usuarioRepository;
    private final TokenVerificacionEmailRepository tokenRepository;
    private final EmailService emailService;
    private final String frontendBaseUrl;

    public ViajeroAuthService(UsuarioViajeroRepository usuarioRepository,
                              TokenVerificacionEmailRepository tokenRepository,
                              EmailService emailService,
                              @Value("${app.frontend.base-url:http://localhost:5500}") String frontendBaseUrl) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.frontendBaseUrl = frontendBaseUrl.replaceAll("/$", "");
    }

    public Map<String, Object> registrar(String email, String password, String nombreCompleto,
                                         String rut, String telefono, String nacionalidad) throws SQLException {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches()) {
            return Map.of("error", "Correo electrónico no válido");
        }
        if (password == null || password.length() < 8) {
            return Map.of("error", "La contraseña debe tener al menos 8 caracteres");
        }
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return Map.of("error", "Indique su nombre completo");
        }

        String emailNorm = email.trim().toLowerCase();
        if (usuarioRepository.findByEmail(emailNorm).isPresent()) {
            return Map.of("error", "Ya existe una cuenta con este correo");
        }

        Long id = usuarioRepository.insert(
                emailNorm,
                PasswordUtil.hash(password),
                nombreCompleto.trim(),
                blankToNull(rut),
                blankToNull(telefono),
                blankToNull(nacionalidad)
        );

        String tokenPlano = generarTokenSeguro();
        tokenRepository.insert(id, hashToken(tokenPlano), LocalDateTime.now().plusHours(24));

        String enlace = frontendBaseUrl + "/verificar-correo.html?token=" + tokenPlano;
        emailService.enviarVerificacion(emailNorm, enlace);

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Cuenta creada. Revise su correo para activar la cuenta.");
        response.put("usuarioId", id);
        response.put("email", emailNorm);
        response.put("enlaceVerificacion", enlace);
        response.put("nota", "Si no recibe el correo, use el enlace de verificación mostrado aquí (modo desarrollo).");
        return response;
    }

    public Map<String, Object> verificarEmail(String token) throws SQLException {
        if (token == null || token.isBlank()) {
            return Map.of("error", "Token no válido");
        }
        String tokenHash = hashToken(token);
        Optional<Long> usuarioIdOpt = tokenRepository.findUsuarioIdByTokenHash(tokenHash);
        if (usuarioIdOpt.isEmpty()) {
            return Map.of("error", "Enlace inválido o expirado");
        }
        Long usuarioId = usuarioIdOpt.get();
        usuarioRepository.marcarEmailVerificado(usuarioId);
        tokenRepository.marcarUsado(tokenHash);
        return Map.of("mensaje", "Correo verificado correctamente. Ya puede iniciar sesión.");
    }

    public Optional<Map<String, Object>> login(String email, String password) throws SQLException {
        Optional<UsuarioViajero> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }
        UsuarioViajero usuario = usuarioOpt.get();
        if (!usuario.isActivo() || !PasswordUtil.verify(password, usuario.getPasswordHash())) {
            return Optional.empty();
        }
        if (!usuario.isEmailVerificado()) {
            Map<String, Object> pendiente = new HashMap<>();
            pendiente.put("error", "Debe verificar su correo antes de ingresar");
            pendiente.put("codigo", "EMAIL_NO_VERIFICADO");
            pendiente.put("email", usuario.getEmail());
            return Optional.of(pendiente);
        }

        String token = AuthTokenFilter.crearTokenViajero(usuario);
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("rol", "VIAJERO");
        response.put("nombre", usuario.getNombreCompleto());
        response.put("usuarioId", usuario.getId());
        response.put("email", usuario.getEmail());
        return Optional.of(response);
    }

    public Map<String, Object> obtenerPerfil(Long viajeroId) throws SQLException {
        Optional<UsuarioViajero> usuarioOpt = usuarioRepository.findById(viajeroId);
        if (usuarioOpt.isEmpty()) {
            return Map.of("error", "Usuario no encontrado");
        }
        UsuarioViajero u = usuarioOpt.get();
        Map<String, Object> perfil = new HashMap<>();
        perfil.put("id", u.getId());
        perfil.put("email", u.getEmail());
        perfil.put("nombreCompleto", u.getNombreCompleto());
        perfil.put("rut", u.getRut());
        perfil.put("telefono", u.getTelefono());
        perfil.put("nacionalidad", u.getNacionalidad());
        perfil.put("emailVerificado", u.isEmailVerificado());
        perfil.put("creadoEn", u.getCreadoEn() != null ? u.getCreadoEn().toString() : null);
        return perfil;
    }

    public Map<String, Object> actualizarPerfil(Long viajeroId, Map<String, String> body) throws SQLException {
        String nombre = body.get("nombreCompleto");
        if (nombre == null || nombre.isBlank()) {
            return Map.of("error", "Nombre completo requerido");
        }
        usuarioRepository.updatePerfil(
                viajeroId,
                nombre.trim(),
                blankToNull(body.get("rut")),
                blankToNull(body.get("telefono")),
                blankToNull(body.get("nacionalidad"))
        );
        return obtenerPerfil(viajeroId);
    }

    public Map<String, Object> cambiarPassword(Long viajeroId, String actual, String nueva) throws SQLException {
        if (nueva == null || nueva.length() < 8) {
            return Map.of("error", "La nueva contraseña debe tener al menos 8 caracteres");
        }
        Optional<UsuarioViajero> usuarioOpt = usuarioRepository.findById(viajeroId);
        if (usuarioOpt.isEmpty()) {
            return Map.of("error", "Usuario no encontrado");
        }
        if (!PasswordUtil.verify(actual, usuarioOpt.get().getPasswordHash())) {
            return Map.of("error", "Contraseña actual incorrecta");
        }
        usuarioRepository.updatePassword(viajeroId, PasswordUtil.hash(nueva));
        return Map.of("mensaje", "Contraseña actualizada");
    }

    public Map<String, Object> reenviarVerificacion(String email) throws SQLException {
        Optional<UsuarioViajero> usuarioOpt = usuarioRepository.findByEmail(email);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Si el correo está registrado y pendiente de verificación, recibirá un nuevo enlace.");
        if (usuarioOpt.isEmpty() || usuarioOpt.get().isEmailVerificado()) {
            return response;
        }
        UsuarioViajero u = usuarioOpt.get();
        String tokenPlano = generarTokenSeguro();
        tokenRepository.insert(u.getId(), hashToken(tokenPlano), LocalDateTime.now().plusHours(24));
        String enlace = frontendBaseUrl + "/verificar-correo.html?token=" + tokenPlano;
        emailService.enviarVerificacion(u.getEmail(), enlace);
        response.put("enlaceVerificacion", enlace);
        return response;
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
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
