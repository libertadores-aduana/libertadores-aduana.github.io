package cl.aduana.libertadores.model;

import java.time.LocalDate;

public class Pasajero {

    private Long id;
    private String documentoEncriptado;
    private String tipoDocumento;
    private String nombresEncriptado;
    private String apellidosEncriptado;
    private LocalDate fechaNacimiento;
    private String nacionalidad;
    private boolean menorEdad;
    private Long tutorId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDocumentoEncriptado() { return documentoEncriptado; }
    public void setDocumentoEncriptado(String documentoEncriptado) { this.documentoEncriptado = documentoEncriptado; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNombresEncriptado() { return nombresEncriptado; }
    public void setNombresEncriptado(String nombresEncriptado) { this.nombresEncriptado = nombresEncriptado; }
    public String getApellidosEncriptado() { return apellidosEncriptado; }
    public void setApellidosEncriptado(String apellidosEncriptado) { this.apellidosEncriptado = apellidosEncriptado; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }
    public boolean isMenorEdad() { return menorEdad; }
    public void setMenorEdad(boolean menorEdad) { this.menorEdad = menorEdad; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
}
