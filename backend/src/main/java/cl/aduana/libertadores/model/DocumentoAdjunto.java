package cl.aduana.libertadores.model;

import java.time.LocalDateTime;

public class DocumentoAdjunto {

    private Long id;
    private Long preRegistroId;
    private Long pasajeroId;
    private String tipoDocumento;
    private String nombreArchivo;
    private String mimeType;
    private Long tamanoBytes;
    private LocalDateTime subidoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPreRegistroId() { return preRegistroId; }
    public void setPreRegistroId(Long preRegistroId) { this.preRegistroId = preRegistroId; }
    public Long getPasajeroId() { return pasajeroId; }
    public void setPasajeroId(Long pasajeroId) { this.pasajeroId = pasajeroId; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }
    public LocalDateTime getSubidoEn() { return subidoEn; }
    public void setSubidoEn(LocalDateTime subidoEn) { this.subidoEn = subidoEn; }
}
