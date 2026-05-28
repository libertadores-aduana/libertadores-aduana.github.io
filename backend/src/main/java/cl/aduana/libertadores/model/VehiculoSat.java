package cl.aduana.libertadores.model;

import java.time.LocalDate;

public class VehiculoSat {

    private Long id;
    private String patenteEncriptada;
    private String paisOrigen;
    private String tipoVehiculo;
    private boolean diplomatico;
    private LocalDate fechaIngreso;
    private LocalDate fechaMaxSalida;
    private boolean encargoRobo;
    private String estadoSat;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPatenteEncriptada() { return patenteEncriptada; }
    public void setPatenteEncriptada(String patenteEncriptada) { this.patenteEncriptada = patenteEncriptada; }
    public String getPaisOrigen() { return paisOrigen; }
    public void setPaisOrigen(String paisOrigen) { this.paisOrigen = paisOrigen; }
    public String getTipoVehiculo() { return tipoVehiculo; }
    public void setTipoVehiculo(String tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }
    public boolean isDiplomatico() { return diplomatico; }
    public void setDiplomatico(boolean diplomatico) { this.diplomatico = diplomatico; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public LocalDate getFechaMaxSalida() { return fechaMaxSalida; }
    public void setFechaMaxSalida(LocalDate fechaMaxSalida) { this.fechaMaxSalida = fechaMaxSalida; }
    public boolean isEncargoRobo() { return encargoRobo; }
    public void setEncargoRobo(boolean encargoRobo) { this.encargoRobo = encargoRobo; }
    public String getEstadoSat() { return estadoSat; }
    public void setEstadoSat(String estadoSat) { this.estadoSat = estadoSat; }
}
