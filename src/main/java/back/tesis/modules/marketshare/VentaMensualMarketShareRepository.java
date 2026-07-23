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
            GROUP BY v.anio, v.mes
            ORDER BY v.anio, v.mes
            """)
    List<OverviewRow> findMarketShareEvolucion(@Param("empresaId") String empresaId);

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
        Double getPrecioUnitario();
        Double getPriceGapPct();
        int getAnio();
        int getMes();
    }

    @Query("""
            SELECT v.productoId AS productoId, v.precioUnitario AS precioUnitario,
                   v.priceGapPct AS priceGapPct, v.anio AS anio, v.mes AS mes
            FROM VentaMensual v
            WHERE v.empresaId = :empresaId
              AND v.priceGapPct IS NOT NULL
              AND v.id IN (
                  SELECT MAX(v2.id) FROM VentaMensual v2
                  WHERE v2.empresaId = :empresaId AND v2.priceGapPct IS NOT NULL
                  GROUP BY v2.productoId
              )
            """)
    List<PriceGapRow> findUltimoPriceGapPorProducto(@Param("empresaId") String empresaId);

    @Query("""
            SELECT AVG(v.priceGapPct)
            FROM VentaMensual v
            WHERE v.empresaId = :empresaId AND v.anio = :anio AND v.mes = :mes
              AND v.priceGapPct IS NOT NULL
            """)
    Double findPriceGapPctPromedioPorPeriodo(
            @Param("empresaId") String empresaId, @Param("anio") int anio, @Param("mes") int mes);
}
