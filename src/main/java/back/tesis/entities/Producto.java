package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto extends UuidEntity {

    @Column(name = "id_marca", nullable = false)
    private String marcaId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "contenido_neto", nullable = false)
    private float contenidoNeto;

    @Column(name = "unidad_medida", nullable = false)
    private String unidadMedida;

    @Column(name = "valor_unitario")
    private BigDecimal valorUnitario;

    protected Producto() {
    }

    public String getMarcaId() {
        return marcaId;
    }

    public String getSku() {
        return sku;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public float getContenidoNeto() {
        return contenidoNeto;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }
}
