package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "config_sistema")
public class ConfigSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_empresa", nullable = false)
    private String empresaId;

    @Column(name = "id_producto")
    private String productoId;

    @Column(nullable = false)
    private String parametro;

    @Column(nullable = false, columnDefinition = "text")
    private String valor;

    protected ConfigSistema() {
    }

    public ConfigSistema(String empresaId, String productoId, String parametro, String valor) {
        this.empresaId = empresaId;
        this.productoId = productoId;
        this.parametro = parametro;
        this.valor = valor;
    }

    public Integer getId() {
        return id;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public String getProductoId() {
        return productoId;
    }

    public String getParametro() {
        return parametro;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
