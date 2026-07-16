package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "simulaciones")
public class Simulacion extends UuidEntity {

    @Column(name = "id_empresa", nullable = false)
    private String empresaId;

    @Column(name = "id_usuario", nullable = false)
    private String usuarioId;

    @Column(name = "id_modelo", nullable = false)
    private String modeloId;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "parametros_entrada", nullable = false, columnDefinition = "json")
    private String parametrosEntrada;

    @Column(nullable = false, columnDefinition = "json")
    private String resultado;

    protected Simulacion() {
    }

    public Simulacion(String empresaId, String usuarioId, String modeloId, String nombre,
                       String parametrosEntrada, String resultado) {
        this.empresaId = empresaId;
        this.usuarioId = usuarioId;
        this.modeloId = modeloId;
        this.nombre = nombre;
        this.parametrosEntrada = parametrosEntrada;
        this.resultado = resultado;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getResultado() {
        return resultado;
    }
}
