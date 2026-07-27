package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs_acceso")
public class LogAcceso extends UuidEntity {

    public static final String RESULTADO_OK = "OK";
    public static final String RESULTADO_DENEGADO = "DENEGADO";
    public static final String RESULTADO_ERROR = "ERROR";

    @Column(name = "id_usuario", nullable = false)
    private String usuarioId;

    @Column(name = "id_empresa", nullable = false)
    private String empresaId;

    @Column(nullable = false)
    private String accion;

    @Column(nullable = false)
    private String endpoint;

    @Column(name = "ip_origen", nullable = false)
    private String ipOrigen;

    @Column(nullable = false)
    private String resultado;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected LogAcceso() {
    }

    public LogAcceso(String usuarioId, String empresaId, String accion, String endpoint, String ipOrigen, String resultado) {
        this.usuarioId = usuarioId;
        this.empresaId = empresaId;
        this.accion = accion;
        this.endpoint = endpoint;
        this.ipOrigen = ipOrigen;
        this.resultado = resultado;
        this.fecha = LocalDateTime.now();
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public String getAccion() {
        return accion;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public String getResultado() {
        return resultado;
    }
}
