package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "archivos_ingesta")
public class ArchivoIngesta extends UuidEntity {

    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_PROCESANDO = "PROCESANDO";
    public static final String ESTADO_OK = "OK";
    public static final String ESTADO_ERROR = "ERROR";
    public static final String ESTADO_ELIMINADO = "ELIMINADO";

    @Column(name = "id_empresa", nullable = false)
    private String empresaId;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "hash_sha256", nullable = false)
    private String hashSha256;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String granularidad;

    @Column(name = "periodo_desde", nullable = false)
    private LocalDate periodoDesde;

    @Column(name = "periodo_hasta", nullable = false)
    private LocalDate periodoHasta;

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDateTime fechaRecepcion;

    protected ArchivoIngesta() {
    }

    public ArchivoIngesta(String empresaId, String nombreArchivo, String hashSha256,
                           String granularidad, LocalDate periodoDesde, LocalDate periodoHasta) {
        this.empresaId = empresaId;
        this.nombreArchivo = nombreArchivo;
        this.hashSha256 = hashSha256;
        this.estado = ESTADO_PENDIENTE;
        this.granularidad = granularidad;
        this.periodoDesde = periodoDesde;
        this.periodoHasta = periodoHasta;
        this.fechaRecepcion = LocalDateTime.now();
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getHashSha256() {
        return hashSha256;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getGranularidad() {
        return granularidad;
    }

    public LocalDate getPeriodoDesde() {
        return periodoDesde;
    }

    public LocalDate getPeriodoHasta() {
        return periodoHasta;
    }

    public LocalDateTime getFechaRecepcion() {
        return fechaRecepcion;
    }
}
