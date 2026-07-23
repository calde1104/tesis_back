package back.tesis.modules.dashboard;

import back.tesis.entities.VentaMensual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MarketShareCompositionRepository extends JpaRepository<VentaMensual, Long> {

    interface CompositionRow {
        String getEmpresaId();
        Double getMarketShareUnidadesPromedio();
    }

    @Query("""
            SELECT v.empresaId AS empresaId, AVG(v.marketShareUnidades) AS marketShareUnidadesPromedio
            FROM VentaMensual v
            WHERE v.anio = :anio AND v.mes = :mes AND v.marketShareUnidades IS NOT NULL
              AND v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv
                  WHERE pv.vigenciaHasta IS NULL
                    AND pv.categoria.id IN (
                        SELECT DISTINCT pv2.categoria.id
                        FROM ProductoVersion pv2
                        WHERE pv2.vigenciaHasta IS NULL
                          AND pv2.producto.id IN (
                              SELECT DISTINCT v2.productoId FROM VentaMensual v2
                              WHERE v2.empresaId = :empresaId AND v2.anio = :anio AND v2.mes = :mes
                          )
                    )
              )
            GROUP BY v.empresaId
            ORDER BY marketShareUnidadesPromedio DESC
            """)
    List<CompositionRow> findComposicionPorPeriodo(
            @Param("empresaId") String empresaId, @Param("anio") int anio, @Param("mes") int mes);
}
