package back.tesis.modules.marketshare;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import back.tesis.entities.VentaMensual;

public interface VentaMensualMarketShareRepository extends JpaRepository<VentaMensual, Long> {

    interface OverviewRow {
        int getAnio();
        int getMes();
        Double getMarketShareUnidadesPromedio();
    }

    @Query("""
            SELECT v.anio AS anio, v.mes AS mes, AVG(v.marketShareUnidades) AS marketShareUnidadesPromedio
            FROM VentaMensual v
            WHERE v.empresaId = :empresaId AND v.marketShareUnidades IS NOT NULL
              AND (:marcaId IS NULL OR v.productoId IN (
                  SELECT p.id FROM Producto p WHERE p.marcaId = :marcaId
              ))
              AND (:categoriaId IS NULL OR v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv WHERE pv.categoria.id = :categoriaId
              ))
              AND (:departamentoId IS NULL OR v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv WHERE pv.categoria.departamento.id = :departamentoId
              ))
              AND (:anios IS NULL OR v.anio IN :anios)
              AND (:meses IS NULL OR v.mes IN :meses)
            GROUP BY v.anio, v.mes
            ORDER BY v.anio, v.mes
            """)
    List<OverviewRow> findMarketShareEvolucion(
            @Param("empresaId") String empresaId,
            @Param("marcaId") String marcaId,
            @Param("categoriaId") Integer categoriaId,
            @Param("departamentoId") Integer departamentoId,
            @Param("anios") List<Integer> anios,
            @Param("meses") List<Integer> meses);

    @Query("""
            SELECT AVG(v.marketShareUnidades)
            FROM VentaMensual v
            WHERE v.empresaId = :empresaId AND v.anio = :anio AND v.mes = :mes
              AND v.marketShareUnidades IS NOT NULL
            """)
    Double findMarketShareUnidadesPromedioPorPeriodo(
            @Param("empresaId") String empresaId, @Param("anio") int anio, @Param("mes") int mes);

    interface PriceGapRow {
        String getProductoId();
        String getNombreProducto();
        Double getPrecioUnitario();
        Double getPriceGapPct();
        int getAnio();
        int getMes();
    }

    @Query("""
            SELECT v.productoId AS productoId,
                   (SELECT p.descripcion FROM Producto p WHERE p.id = v.productoId) AS nombreProducto,
                   v.precioUnitario AS precioUnitario,
                   v.priceGapPct AS priceGapPct, v.anio AS anio, v.mes AS mes
            FROM VentaMensual v
            WHERE v.empresaId = :empresaId
              AND v.priceGapPct IS NOT NULL
              AND (:marcaId IS NULL OR v.productoId IN (
                  SELECT p.id FROM Producto p WHERE p.marcaId = :marcaId
              ))
              AND (:categoriaId IS NULL OR v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv WHERE pv.categoria.id = :categoriaId
              ))
              AND (:departamentoId IS NULL OR v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv WHERE pv.categoria.departamento.id = :departamentoId
              ))
              AND (:anios IS NULL OR v.anio IN :anios)
              AND (:meses IS NULL OR v.mes IN :meses)
              AND v.id IN (
                  SELECT MAX(v2.id) FROM VentaMensual v2
                  WHERE v2.empresaId = :empresaId AND v2.priceGapPct IS NOT NULL
                  GROUP BY v2.productoId
              )
            """)
    List<PriceGapRow> findUltimoPriceGapPorProducto(
            @Param("empresaId") String empresaId,
            @Param("marcaId") String marcaId,
            @Param("categoriaId") Integer categoriaId,
            @Param("departamentoId") Integer departamentoId,
            @Param("anios") List<Integer> anios,
            @Param("meses") List<Integer> meses);

    @Query("""
            SELECT AVG(v.priceGapPct)
            FROM VentaMensual v
            WHERE v.empresaId = :empresaId AND v.anio = :anio AND v.mes = :mes
              AND v.priceGapPct IS NOT NULL
            """)
    Double findPriceGapPctPromedioPorPeriodo(
            @Param("empresaId") String empresaId, @Param("anio") int anio, @Param("mes") int mes);
}
