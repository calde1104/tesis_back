package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "ventas_semanales")
public class VentaSemanal {

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
    private int semana;

    @Column(name = "unidades_vendidas", nullable = false)
    private int unidadesVendidas;

    @Column(name = "monto_facturado", nullable = false)
    private BigDecimal montoFacturado;

    protected VentaSemanal() {
    }

    public Long getId() {
        return id;
    }
}
