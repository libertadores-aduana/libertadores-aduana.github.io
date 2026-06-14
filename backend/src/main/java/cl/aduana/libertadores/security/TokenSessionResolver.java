package cl.aduana.libertadores.security;

import cl.aduana.libertadores.model.Empleado;
import cl.aduana.libertadores.model.UsuarioViajero;
import cl.aduana.libertadores.repository.EmpleadoRepository;
import cl.aduana.libertadores.repository.UsuarioViajeroRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * Reconstruye la sesión desde el token cuando el mapa en memoria se pierde (reinicio en Render).
 */
@Service
public class TokenSessionResolver {

    private final EmpleadoRepository empleadoRepository;
    private final UsuarioViajeroRepository usuarioViajeroRepository;

    public TokenSessionResolver(EmpleadoRepository empleadoRepository,
                                UsuarioViajeroRepository usuarioViajeroRepository) {
        this.empleadoRepository = empleadoRepository;
        this.usuarioViajeroRepository = usuarioViajeroRepository;
    }

    public Optional<SessionContext> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            int colon = decoded.indexOf(':');
            if (colon <= 1) {
                return Optional.empty();
            }
            char tipo = decoded.charAt(0);
            long id = Long.parseLong(decoded.substring(1, colon));

            if (tipo == 'E') {
                Optional<Empleado> empleado = empleadoRepository.findById(id);
                if (empleado.isEmpty() || !empleado.get().isActivo()) {
                    return Optional.empty();
                }
                return Optional.of(SessionContext.empleado(empleado.get()));
            }
            if (tipo == 'V') {
                Optional<UsuarioViajero> viajero = usuarioViajeroRepository.findById(id);
                if (viajero.isEmpty() || !viajero.get().isActivo()) {
                    return Optional.empty();
                }
                return Optional.of(SessionContext.viajero(viajero.get()));
            }
        } catch (Exception ignored) {
            return Optional.empty();
        }
        return Optional.empty();
    }
}
