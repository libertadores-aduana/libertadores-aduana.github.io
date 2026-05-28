package cl.aduana.libertadores.security;

import cl.aduana.libertadores.model.RolInstitucion;

public class SessionContext {

    private final Long empleadoId;
    private final RolInstitucion rol;
    private final String nombre;

    public SessionContext(Long empleadoId, RolInstitucion rol, String nombre) {
        this.empleadoId = empleadoId;
        this.rol = rol;
        this.nombre = nombre;
    }

    public Long getEmpleadoId() { return empleadoId; }
    public RolInstitucion getRol() { return rol; }
    public String getNombre() { return nombre; }
}
