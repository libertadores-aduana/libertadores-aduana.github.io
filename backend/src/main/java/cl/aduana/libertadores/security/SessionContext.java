package cl.aduana.libertadores.security;

import cl.aduana.libertadores.model.Empleado;
import cl.aduana.libertadores.model.RolInstitucion;
import cl.aduana.libertadores.model.UsuarioViajero;

public class SessionContext {

    private final Long empleadoId;
    private final Long viajeroId;
    private final RolInstitucion rol;
    private final String nombre;
    private final String email;

    private SessionContext(Long empleadoId, Long viajeroId, RolInstitucion rol, String nombre, String email) {
        this.empleadoId = empleadoId;
        this.viajeroId = viajeroId;
        this.rol = rol;
        this.nombre = nombre;
        this.email = email;
    }

    public static SessionContext empleado(Empleado empleado) {
        return new SessionContext(
                empleado.getId(),
                null,
                empleado.getRol(),
                empleado.getNombreCompleto(),
                empleado.getEmail()
        );
    }

    public static SessionContext viajero(UsuarioViajero usuario) {
        return new SessionContext(
                null,
                usuario.getId(),
                RolInstitucion.VIAJERO,
                usuario.getNombreCompleto(),
                usuario.getEmail()
        );
    }

    public boolean esViajero() {
        return rol == RolInstitucion.VIAJERO;
    }

    public boolean esEmpleado() {
        return empleadoId != null;
    }

    public Long getEmpleadoId() { return empleadoId; }
    public Long getViajeroId() { return viajeroId; }
    public RolInstitucion getRol() { return rol; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
}
