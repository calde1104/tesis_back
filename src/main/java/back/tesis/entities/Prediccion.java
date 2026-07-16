package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "predicciones")
public class Prediccion extends UuidEntity {

    @Column(name = "id_empresa", nullable = false)
    private String empresaId;

    @Column(name = "id_producto", nullable = false)
    private String productoId;

    @Column(name = "id_modelo", nullable = false)
    private String modeloId;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int mes;

    @Column(name = "unidades_predichas", nullable = false)
    private float unidadesPredichas;

    @Column(name = "monto_predicho", nullable = false)
    private BigDecimal montoPredicho;

    @Column(name = "intervalo_confianza_inf", nullable = false)
    private float intervaloConfianzaInf;

    @Column(name = "intervalo_confianza_sup", nullable = false)
    private float intervaloConfianzaSup;

    protected Prediccion() {
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public String getProductoId() {
        return productoId;
    }

    public int getAnio() {
        return anio;
    }

    public int getMes() {
        return mes;
    }

    public float getUnidadesPredichas() {
        return unidadesPredichas;
    }

    public BigDecimal getMontoPredicho() {
        return montoPredicho;
    }
}
