package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_sistema")
public class EventoSistema extends UuidEntity {

    public static final String SEVERIDAD_INFO = "INFO";
    public static final String SEVERIDAD_WARNING = "WARNING";
    public static final String SEVERIDAD_CRITICO = "CRITICO";

    public static final String ESTADO_ACTIVA = "ACTIVA";
    public static final String ESTADO_VISTA = "VISTA";
    public static final String ESTADO_RESUELTA = "RESUELTA";

    @Column(name = "id_empresa", nullable = false)
    private String empresaId;

    @Column(name = "id_producto")
    private String productoId;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(nullable = false)
    private String severidad;

    @Column(nullable = false, columnDefinition = "json")
    private String payload;

    @Column(nullable = false)
    private String estado;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "resuelto_por")
    private String resueltoPor;

    protected EventoSistema() {
    }

    public EventoSistema(String empresaId, String productoId, String tipoEvento, String severidad, String payload) {
        this.empresaId = empresaId;
        this.productoId = productoId;
        this.tipoEvento = tipoEvento;
        this.severidad = severidad;
        this.payload = payload;
        this.estado = ESTADO_ACTIVA;
        this.fechaGeneracion = LocalDateTime.now();
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public String getProductoId() {
        return productoId;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public String getSeveridad() {
        return severidad;
    }

    public String getPayload() {
        return payload;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDateTime fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public String getResueltoPor() {
        return resueltoPor;
    }

    public void setResueltoPor(String resueltoPor) {
        this.resueltoPor = resueltoPor;
    }
}
