package back.tesis.modules.sales;

import back.tesis.entities.VentaMensual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SalesRepository extends JpaRepository<VentaMensual, Long> {

    interface SaleRow {
        String getProductoId();
        int getAnio();
        int getMes();
        int getUnidadesVendidas();
        BigDecimal getMontoFacturado();
        BigDecimal getPrecioUnitario();
        String getSku();
        String getDescripcionProducto();
        String getMarcaNombre();
        String getCategoriaNombre();
        Float getContenidoNeto();
        String getUnidadMedida();
    }

    @Query("""
            SELECT
                v.productoId AS productoId, v.anio AS anio, v.mes AS mes,
                v.unidadesVendidas AS unidadesVendidas, v.montoFacturado AS montoFacturado,
                v.precioUnitario AS precioUnitario,
                (SELECT p.sku FROM Producto p WHERE p.id = v.productoId) AS sku,
                (SELECT p.descripcion FROM Producto p WHERE p.id = v.productoId) AS descripcionProducto,
                (SELECT m.nombre FROM Producto p JOIN Marca m ON m.id = p.marcaId WHERE p.id = v.productoId) AS marcaNombre,
                (SELECT pv.categoria.nombre FROM ProductoVersion pv WHERE pv.producto.id = v.productoId AND pv.vigenciaHasta IS NULL) AS categoriaNombre,
                (SELECT p.contenidoNeto FROM Producto p WHERE p.id = v.productoId) AS contenidoNeto,
                (SELECT p.unidadMedida FROM Producto p WHERE p.id = v.productoId) AS unidadMedida
            FROM VentaMensual v
            WHERE v.empresaId = :empresaId
              AND v.anio IN :anios
              AND (:meses IS NULL OR v.mes IN :meses)
              AND (:marcaId IS NULL OR v.productoId IN (
                  SELECT p.id FROM Producto p WHERE p.marcaId = :marcaId
              ))
              AND (:categoriaId IS NULL OR v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv WHERE pv.categoria.id = :categoriaId
              ))
              AND (:departamentoId IS NULL OR v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv WHERE pv.categoria.departamento.id = :departamentoId
              ))
            ORDER BY v.anio DESC, v.mes DESC
            """)
    Page<SaleRow> findSales(
            @Param("empresaId") String empresaId,
            @Param("anios") List<Integer> anios,
            @Param("meses") List<Integer> meses,
            @Param("marcaId") String marcaId,
            @Param("categoriaId") Integer categoriaId,
            @Param("departamentoId") Integer departamentoId,
            Pageable pageable
    );

    interface SummaryRow {
        Long getTotalUnidades();
        BigDecimal getTotalFacturado();
    }

    @Query("""
            SELECT SUM(v.unidadesVendidas) AS totalUnidades, SUM(v.montoFacturado) AS totalFacturado
            FROM VentaMensual v
            WHERE v.empresaId = :empresaId
              AND (:anio IS NULL OR v.anio = :anio)
              AND (:mes IS NULL OR v.mes = :mes)
            """)
    SummaryRow findSummary(
            @Param("empresaId") String empresaId,
            @Param("anio") Integer anio,
            @Param("mes") Integer mes
    );

    interface MonthlyRevenueRow {
        Integer getAnio();
        Integer getMes();
        BigDecimal getMontoFacturado();
    }

    @Query("""
            SELECT v.anio AS anio, v.mes AS mes, SUM(v.montoFacturado) AS montoFacturado
            FROM VentaMensual v
            WHERE v.empresaId = :empresaId
            GROUP BY v.anio, v.mes
            ORDER BY v.anio, v.mes
            """)
    List<MonthlyRevenueRow> findMonthlyRevenue(@Param("empresaId") String empresaId);
}
