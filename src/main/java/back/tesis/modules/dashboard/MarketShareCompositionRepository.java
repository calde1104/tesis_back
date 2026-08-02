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
            SELECT DISTINCT pv.categoria.id
            FROM ProductoVersion pv
            WHERE pv.vigenciaHasta IS NULL
              AND pv.producto.id IN (
                  SELECT DISTINCT v.productoId FROM VentaMensual v
                  WHERE v.empresaId = :empresaId AND v.anio = :anio AND v.mes = :mes
              )
            """)
    List<Integer> findCategoriaIdsDondeCompiteEmpresa(
            @Param("empresaId") String empresaId, @Param("anio") int anio, @Param("mes") int mes);

    @Query("""
            SELECT v.empresaId AS empresaId, SUM(v.unidadesVendidas) AS totalUnidades
            FROM VentaMensual v
            WHERE v.anio = :anio AND v.mes = :mes
              AND v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv
                  WHERE pv.vigenciaHasta IS NULL AND pv.categoria.id IN :categoriaIds
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
            @Param("anio") int anio, @Param("mes") int mes, @Param("categoriaIds") List<Integer> categoriaIds,
            @Param("marcaId") String marcaId, @Param("categoriaId") Integer categoriaId,
            @Param("departamentoId") Integer departamentoId);
}
