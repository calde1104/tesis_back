package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "ventas_mensuales")
public class VentaMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_producto", nullable = false)
    private String productoId;

    @Column(name = "id_archivo", nullable = false)
    private String archivoId;

    @Column(name = "id_empresa", nullable = false)
    private String empresaId;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int mes;

    @Column(name = "unidades_vendidas", nullable = false)
    private int unidadesVendidas;

    @Column(name = "monto_facturado", nullable = false)
    private BigDecimal montoFacturado;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    @Column(name = "lag_1")
    private Integer lag1;

    @Column(name = "lag_2")
    private Integer lag2;

    @Column(name = "lag_12")
    private Integer lag12;

    @Column(name = "ma_3m")
    private Float ma3m;

    @Column(name = "market_share_unidades")
    private Float marketShareUnidades;

    @Column(name = "price_gap_pct")
    private Float priceGapPct;

    @Column(name = "es_agregado", nullable = false)
    private boolean esAgregado;

    protected VentaMensual() {
    }

    public VentaMensual(String productoId, String archivoId, String empresaId, int anio, int mes,
                         int unidadesVendidas, BigDecimal montoFacturado, BigDecimal precioUnitario) {
        this.productoId = productoId;
        this.archivoId = archivoId;
        this.empresaId = empresaId;
        this.anio = anio;
        this.mes = mes;
        this.unidadesVendidas = unidadesVendidas;
        this.montoFacturado = montoFacturado;
        this.precioUnitario = precioUnitario;
        this.esAgregado = false;
    }

    public Long getId() {
        return id;
    }

    public String getProductoId() {
        return productoId;
    }

    public String getArchivoId() {
        return archivoId;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public int getAnio() {
        return anio;
    }

    public int getMes() {
        return mes;
    }

    public int getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public BigDecimal getMontoFacturado() {
        return montoFacturado;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public Float getMarketShareUnidades() {
        return marketShareUnidades;
    }

    public Float getPriceGapPct() {
        return priceGapPct;
    }
}
