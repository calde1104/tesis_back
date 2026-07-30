package back.tesis.modules.dashboard;

import back.tesis.entities.VentaMensual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MarketShareCompositionRepository extends JpaRepository<VentaMensual, Long> {

    interface CompositionRow {
        String getEmpresaId();
        Long getTotalUnidades();
    }

    @Query("""
            SELECT v.empresaId AS empresaId, SUM(v.unidadesVendidas) AS totalUnidades
            FROM VentaMensual v
            WHERE v.anio = :anio AND v.mes = :mes
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
              AND (:marcaId IS NULL OR v.productoId IN (
                  SELECT p.id FROM Producto p WHERE p.marcaId = :marcaId
              ))
              AND (:categoriaId IS NULL OR v.productoId IN (
                  SELECT pv3.producto.id FROM ProductoVersion pv3 WHERE pv3.categoria.id = :categoriaId
              ))
              AND (:departamentoId IS NULL OR v.productoId IN (
                  SELECT pv4.producto.id FROM ProductoVersion pv4 WHERE pv4.categoria.departamento.id = :departamentoId
              ))
            GROUP BY v.empresaId
            ORDER BY totalUnidades DESC
            """)
    List<CompositionRow> findComposicionPorPeriodo(
            @Param("empresaId") String empresaId, @Param("anio") int anio, @Param("mes") int mes,
            @Param("marcaId") String marcaId, @Param("categoriaId") Integer categoriaId,
            @Param("departamentoId") Integer departamentoId);
}
