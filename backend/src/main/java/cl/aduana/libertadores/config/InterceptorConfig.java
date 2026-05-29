package cl.aduana.libertadores.config;

import cl.aduana.libertadores.security.AuthTokenFilter;
import cl.aduana.libertadores.security.RbacInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final RbacInterceptor rbacInterceptor;
    private final AuthTokenFilter authTokenFilter;

    public InterceptorConfig(RbacInterceptor rbacInterceptor, AuthTokenFilter authTokenFilter) {
        this.rbacInterceptor = rbacInterceptor;
        this.authTokenFilter = authTokenFilter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] publicPaths = {
                "/api/auth/login",
                "/api/auth/recuperar",
                "/api/auth/restablecer",
                "/api/auth/viajero/registro",
                "/api/auth/viajero/login",
                "/api/auth/viajero/verificar",
                "/api/auth/viajero/reenviar-verificacion",
                "/api/health",
                "/api/health/**",
                "/api/public/**"
        };
        registry.addInterceptor(authTokenFilter)
                .addPathPatterns("/api/**")
                .excludePathPatterns(publicPaths);
        registry.addInterceptor(rbacInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(publicPaths);
    }
}
