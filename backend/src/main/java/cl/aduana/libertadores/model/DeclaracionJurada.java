package cl.aduana.libertadores.model;

import java.time.LocalDateTime;

public class DeclaracionJurada {

    private Long id;
    private Long pasajeroId;
    private Long representanteId;
    private boolean productosAgropecuarios;
    private boolean animalesMascotas;
    private String detalle;
    private LocalDateTime fechaDeclaracion;
    private Long empleadoSagId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPasajeroId() { return pasajeroId; }
    public void setPasajeroId(Long pasajeroId) { this.pasajeroId = pasajeroId; }
    public Long getRepresentanteId() { return representanteId; }
    public void setRepresentanteId(Long representanteId) { this.representanteId = representanteId; }
    public boolean isProductosAgropecuarios() { return productosAgropecuarios; }
    public void setProductosAgropecuarios(boolean productosAgropecuarios) { this.productosAgropecuarios = productosAgropecuarios; }
    public boolean isAnimalesMascotas() { return animalesMascotas; }
    public void setAnimalesMascotas(boolean animalesMascotas) { this.animalesMascotas = animalesMascotas; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public LocalDateTime getFechaDeclaracion() { return fechaDeclaracion; }
    public void setFechaDeclaracion(LocalDateTime fechaDeclaracion) { this.fechaDeclaracion = fechaDeclaracion; }
    public Long getEmpleadoSagId() { return empleadoSagId; }
    public void setEmpleadoSagId(Long empleadoSagId) { this.empleadoSagId = empleadoSagId; }
}
