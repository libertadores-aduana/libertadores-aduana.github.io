package cl.aduana.libertadores.security;

import cl.aduana.libertadores.model.Empleado;
import cl.aduana.libertadores.model.UsuarioViajero;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Autenticación simple por token en memoria (desarrollo).
 * En producción conviene JWT firmado o sesiones en Redis.
 */
@Component
public class AuthTokenFilter implements HandlerInterceptor {

    public static final Map<String, SessionContext> TOKENS = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            SessionContext ctx = TOKENS.get(token);
            if (ctx != null) {
                request.setAttribute(RbacInterceptor.SESSION_ATTR, ctx);
                return true;
            }
        }
        return true;
    }

    public static String crearToken(Empleado empleado) {
        String token = generarTokenAleatorio(empleado.getId(), "E");
        TOKENS.put(token, SessionContext.empleado(empleado));
        return token;
    }

    public static String crearTokenViajero(UsuarioViajero usuario) {
        String token = generarTokenAleatorio(usuario.getId(), "V");
        TOKENS.put(token, SessionContext.viajero(usuario));
        return token;
    }

    private static String generarTokenAleatorio(Long id, String prefijo) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                (prefijo + id + ":" + System.currentTimeMillis()).getBytes());
    }
}
