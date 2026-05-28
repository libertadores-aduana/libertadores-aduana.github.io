package cl.aduana.libertadores.security;

import cl.aduana.libertadores.model.RolInstitucion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RbacInterceptor implements HandlerInterceptor {

    public static final String SESSION_ATTR = "SESSION_CONTEXT";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        RequireRole annotation = method.getMethodAnnotation(RequireRole.class);
        if (annotation == null) {
            annotation = method.getBeanType().getAnnotation(RequireRole.class);
        }
        if (annotation == null) {
            return true;
        }

        SessionContext session = (SessionContext) request.getAttribute(SESSION_ATTR);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"No autenticado\"}");
            return false;
        }

        Set<RolInstitucion> permitidos = Arrays.stream(annotation.value()).collect(Collectors.toSet());
        if (!permitidos.contains(session.getRol()) && session.getRol() != RolInstitucion.ADMIN) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Acceso denegado para su rol\"}");
            return false;
        }
        return true;
    }
}
