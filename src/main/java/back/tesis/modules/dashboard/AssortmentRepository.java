package back.tesis.modules.dashboard;

import back.tesis.entities.VentaMensual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssortmentRepository extends JpaRepository<VentaMensual, Long> {

    @Query("""
            SELECT DISTINCT v.productoId
            FROM VentaMensual v
            WHERE v.empresaId = :empresaId
              AND (v.anio > :cutoffAnio OR (v.anio = :cutoffAnio AND v.mes >= :cutoffMes))
              AND (v.anio < :anio OR (v.anio = :anio AND v.mes <= :mes))
              AND (:marcaId IS NULL OR v.productoId IN (
                  SELECT p.id FROM Producto p WHERE p.marcaId = :marcaId
              ))
              AND (:categoriaId IS NULL OR v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv WHERE pv.categoria.id = :categoriaId
              ))
              AND (:departamentoId IS NULL OR v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv WHERE pv.categoria.departamento.id = :departamentoId
              ))
            """)
    List<String> findActiveProductoIds(
            @Param("empresaId") String empresaId,
            @Param("cutoffAnio") int cutoffAnio,
            @Param("cutoffMes") int cutoffMes,
            @Param("anio") int anio,
            @Param("mes") int mes,
            @Param("marcaId") String marcaId,
            @Param("categoriaId") Integer categoriaId,
            @Param("departamentoId") Integer departamentoId
    );

    @Query("""
            SELECT DISTINCT pv.categoria.id
            FROM ProductoVersion pv
            WHERE pv.producto.id IN :productoIds AND pv.vigenciaHasta IS NULL
            """)
    List<Integer> findCategoriaIdsForProductos(@Param("productoIds") List<String> productoIds);

    @Query("""
            SELECT COUNT(DISTINCT pv.producto.id)
            FROM ProductoVersion pv
            WHERE pv.categoria.id IN :categoriaIds AND pv.vigenciaHasta IS NULL
            """)
    long countMarketProductosByCategorias(@Param("categoriaIds") List<Integer> categoriaIds);

    interface EmpresaProductoCountRow {
        String getEmpresaId();
        Long getTotalProductos();
    }

    @Query("""
            SELECT v.empresaId AS empresaId, COUNT(DISTINCT v.productoId) AS totalProductos
            FROM VentaMensual v
            WHERE (v.anio > :cutoffAnio OR (v.anio = :cutoffAnio AND v.mes >= :cutoffMes))
              AND (v.anio < :anio OR (v.anio = :anio AND v.mes <= :mes))
              AND v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv
                  WHERE pv.vigenciaHasta IS NULL AND pv.categoria.id IN :categoriaIds
              )
              AND (:marcaId IS NULL OR v.productoId IN (
                  SELECT p.id FROM Producto p WHERE p.marcaId = :marcaId
              ))
              AND (:departamentoId IS NULL OR v.productoId IN (
                  SELECT pv2.producto.id FROM ProductoVersion pv2 WHERE pv2.categoria.departamento.id = :departamentoId
              ))
            GROUP BY v.empresaId
            """)
    List<EmpresaProductoCountRow> countActiveProductosPorEmpresa(
            @Param("cutoffAnio") int cutoffAnio,
            @Param("cutoffMes") int cutoffMes,
            @Param("anio") int anio,
            @Param("mes") int mes,
            @Param("categoriaIds") List<Integer> categoriaIds,
            @Param("marcaId") String marcaId,
            @Param("departamentoId") Integer departamentoId
    );

    @Query("""
            SELECT DISTINCT pv.categoria.id
            FROM ProductoVersion pv
            WHERE pv.vigenciaHasta IS NULL
              AND pv.producto.id IN (SELECT p.id FROM Producto p WHERE p.marcaId = :marcaId)
            """)
    List<Integer> findCategoriaIdsForMarca(@Param("marcaId") String marcaId);

    interface MarcaProductoCountRow {
        String getMarcaId();
        Long getTotalProductos();
    }

    @Query("""
            SELECT p.marcaId AS marcaId, COUNT(DISTINCT v.productoId) AS totalProductos
            FROM VentaMensual v, Producto p
            WHERE v.productoId = p.id
              AND (v.anio > :cutoffAnio OR (v.anio = :cutoffAnio AND v.mes >= :cutoffMes))
              AND (v.anio < :anio OR (v.anio = :anio AND v.mes <= :mes))
              AND v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv
                  WHERE pv.vigenciaHasta IS NULL AND pv.categoria.id IN :categoriaIds
              )
              AND (:departamentoId IS NULL OR v.productoId IN (
                  SELECT pv2.producto.id FROM ProductoVersion pv2 WHERE pv2.categoria.departamento.id = :departamentoId
              ))
            GROUP BY p.marcaId
            """)
    List<MarcaProductoCountRow> countActiveProductosPorMarca(
            @Param("cutoffAnio") int cutoffAnio,
            @Param("cutoffMes") int cutoffMes,
            @Param("anio") int anio,
            @Param("mes") int mes,
            @Param("categoriaIds") List<Integer> categoriaIds,
            @Param("departamentoId") Integer departamentoId
    );
}
