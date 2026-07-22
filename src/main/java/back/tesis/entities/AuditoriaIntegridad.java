package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_integridad")
public class AuditoriaIntegridad extends UuidEntity {

    public static final String RESULTADO_PENDIENTE = "PENDIENTE";
    public static final String RESULTADO_VALIDO = "VALIDO";
    public static final String RESULTADO_INVALIDO = "INVALIDO";
    public static final String RESULTADO_ALTERADO = "ALTERADO";

    @Column(name = "id_archivo", nullable = false, unique = true)
    private String archivoId;

    @Column(name = "hash_sha256", nullable = false)
    private String hashSha256;

    @Column(name = "resultado_validacion", nullable = false)
    private String resultadoValidacion;

    @Column(name = "fecha_sello", nullable = false)
    private LocalDateTime fechaSello;

    protected AuditoriaIntegridad() {
    }

    public AuditoriaIntegridad(String archivoId, String hashSha256, String resultadoValidacion) {
        this.archivoId = archivoId;
        this.hashSha256 = hashSha256;
        this.resultadoValidacion = resultadoValidacion;
    }

    public String getArchivoId() {
        return archivoId;
    }

    public String getHashSha256() {
        return hashSha256;
    }

    public String getResultadoValidacion() {
        return resultadoValidacion;
    }
}
