package cl.aduana.libertadores.model;

import java.time.LocalDate;

public class PermisoMenor {

    private Long id;
    private Long pasajeroMenorId;
    private String tipoPermiso;
    private String numeroDocumento;
    private LocalDate fechaEmision;
    private boolean validado;
    private Long empleadoValidadorId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPasajeroMenorId() { return pasajeroMenorId; }
    public void setPasajeroMenorId(Long pasajeroMenorId) { this.pasajeroMenorId = pasajeroMenorId; }
    public String getTipoPermiso() { return tipoPermiso; }
    public void setTipoPermiso(String tipoPermiso) { this.tipoPermiso = tipoPermiso; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    public boolean isValidado() { return validado; }
    public void setValidado(boolean validado) { this.validado = validado; }
    public Long getEmpleadoValidadorId() { return empleadoValidadorId; }
    public void setEmpleadoValidadorId(Long empleadoValidadorId) { this.empleadoValidadorId = empleadoValidadorId; }
}
