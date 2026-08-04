package back.tesis.modules.marketshare;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import back.tesis.entities.VentaMensual;

public interface VentaMensualMarketShareRepository extends JpaRepository<VentaMensual, Long> {

    interface PeriodoUnidadesRow {
        int getAnio();
        int getMes();
        Long getTotalUnidades();
    }

    @Query("""
            SELECT v.anio AS anio, v.mes AS mes, SUM(v.unidadesVendidas) AS totalUnidades
            FROM VentaMensual v
            WHERE v.empresaId = :empresaId
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
    List<PeriodoUnidadesRow> findUnidadesEmpresaPorPeriodo(
            @Param("empresaId") String empresaId,
            @Param("marcaId") String marcaId,
            @Param("categoriaId") Integer categoriaId,
            @Param("departamentoId") Integer departamentoId,
            @Param("anios") List<Integer> anios,
            @Param("meses") List<Integer> meses);

    @Query("""
            SELECT DISTINCT pv.categoria.id FROM ProductoVersion pv
            WHERE pv.vigenciaHasta IS NULL
              AND pv.producto.id IN (SELECT p.id FROM Producto p WHERE p.marcaId = :marcaId)
            """)
    List<Integer> findCategoriaIdsForMarca(@Param("marcaId") String marcaId);

    @Query("""
            SELECT DISTINCT pv.categoria.id
            FROM ProductoVersion pv
            WHERE pv.vigenciaHasta IS NULL
              AND pv.producto.id IN (
                  SELECT DISTINCT v.productoId FROM VentaMensual v
                  WHERE v.empresaId = :empresaId
                    AND (:anios IS NULL OR v.anio IN :anios)
                    AND (:meses IS NULL OR v.mes IN :meses)
              )
            """)
    List<Integer> findCategoriaIdsDondeCompiteEmpresa(
            @Param("empresaId") String empresaId,
            @Param("anios") List<Integer> anios,
            @Param("meses") List<Integer> meses);

    @Query("""
            SELECT DISTINCT pv.categoria.id
            FROM ProductoVersion pv
            WHERE pv.vigenciaHasta IS NULL
              AND pv.producto.id IN (
                  SELECT DISTINCT v.productoId FROM VentaMensual v
                  WHERE v.empresaId = :empresaId AND v.anio = :anio AND v.mes = :mes
              )
            """)
    List<Integer> findCategoriaIdsDondeCompiteEmpresaEnPeriodo(
            @Param("empresaId") String empresaId, @Param("anio") int anio, @Param("mes") int mes);

    @Query("""
            SELECT DISTINCT pv.categoria.id
            FROM ProductoVersion pv
            WHERE pv.vigenciaHasta IS NULL
              AND pv.producto.id IN (
                  SELECT DISTINCT v.productoId FROM VentaMensual v
                  WHERE v.empresaId = :empresaId AND v.anio = :anio AND v.mes = :mes
              )
              AND (:departamentoId IS NULL OR pv.categoria.departamento.id = :departamentoId)
            """)
    List<Integer> findCategoriaIdsDondeCompiteEmpresaEnPeriodo(
            @Param("empresaId") String empresaId, @Param("anio") int anio, @Param("mes") int mes,
            @Param("departamentoId") Integer departamentoId);

    interface CategoriaEmpresaUnidadesRow {
        Integer getCategoriaId();
        String getEmpresaId();
        Long getTotalUnidades();
    }

    @Query("""
            SELECT pv.categoria.id AS categoriaId, v.empresaId AS empresaId, SUM(v.unidadesVendidas) AS totalUnidades
            FROM VentaMensual v, ProductoVersion pv
            WHERE v.productoId = pv.producto.id
              AND pv.vigenciaHasta IS NULL
              AND v.anio = :anio AND v.mes = :mes
              AND pv.categoria.id IN :categoriaIds
              AND (:departamentoId IS NULL OR pv.categoria.departamento.id = :departamentoId)
            GROUP BY pv.categoria.id, v.empresaId
            """)
    List<CategoriaEmpresaUnidadesRow> findUnidadesPorCategoriaYEmpresa(
            @Param("anio") int anio, @Param("mes") int mes,
            @Param("categoriaIds") List<Integer> categoriaIds,
            @Param("departamentoId") Integer departamentoId);

    @Query("""
            SELECT v.anio AS anio, v.mes AS mes, SUM(v.unidadesVendidas) AS totalUnidades
            FROM VentaMensual v
            WHERE v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv
                  WHERE pv.vigenciaHasta IS NULL AND pv.categoria.id IN :categoriaIds
              )
              AND (:departamentoId IS NULL OR v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv WHERE pv.categoria.departamento.id = :departamentoId
              ))
              AND (:anios IS NULL OR v.anio IN :anios)
              AND (:meses IS NULL OR v.mes IN :meses)
            GROUP BY v.anio, v.mes
            ORDER BY v.anio, v.mes
            """)
    List<PeriodoUnidadesRow> findUnidadesMercadoPorPeriodo(
            @Param("categoriaIds") List<Integer> categoriaIds,
            @Param("departamentoId") Integer departamentoId,
            @Param("anios") List<Integer> anios,
            @Param("meses") List<Integer> meses);

    @Query("""
            SELECT SUM(v.unidadesVendidas)
            FROM VentaMensual v
            WHERE v.empresaId = :empresaId AND v.anio = :anio AND v.mes = :mes
            """)
    Long sumUnidadesEmpresaPorPeriodo(
            @Param("empresaId") String empresaId, @Param("anio") int anio, @Param("mes") int mes);

    @Query("""
            SELECT SUM(v.unidadesVendidas)
            FROM VentaMensual v
            WHERE v.anio = :anio AND v.mes = :mes
              AND v.productoId IN (
                  SELECT pv.producto.id FROM ProductoVersion pv
                  WHERE pv.vigenciaHasta IS NULL AND pv.categoria.id IN :categoriaIds
              )
            """)
    Long sumUnidadesMercadoPorPeriodo(
            @Param("anio") int anio, @Param("mes") int mes, @Param("categoriaIds") List<Integer> categoriaIds);

    interface PriceGapRow {
        String getProductoId();
        String getNombreProducto();
        Double getPrecioUnitario();
        Double getPriceGapPct();
        int getAnio();
        int getMes();
    }

    @Query(nativeQuery = true, value = """
            SELECT ranked.productoId AS productoId,
                   p.descripcion AS nombreProducto,
                   ranked.precioUnitario AS precioUnitario,
                   ranked.priceGapPct AS priceGapPct,
                   ranked.anio AS anio,
                   ranked.mes AS mes
            FROM (
                SELECT t.producto_id AS productoId, t.precio_unitario AS precioUnitario,
                       t.price_gap_pct AS priceGapPct, t.anio AS anio, t.mes AS mes,
                       ROW_NUMBER() OVER (PARTITION BY t.producto_id ORDER BY t.anio DESC, t.mes DESC) AS rn
                FROM (
                    SELECT v.id_producto AS producto_id, v.precio_unitario, v.anio, v.mes,
                           (v.precio_unitario - LAG(v.precio_unitario) OVER (PARTITION BY v.id_producto ORDER BY v.anio, v.mes))
                             / NULLIF(LAG(v.precio_unitario) OVER (PARTITION BY v.id_producto ORDER BY v.anio, v.mes), 0) * 100.0
                             AS price_gap_pct
                    FROM ventas_mensuales v
                    WHERE v.id_empresa = :empresaId
                ) t
                WHERE t.price_gap_pct IS NOT NULL
                  AND (:marcaId IS NULL OR t.producto_id IN (SELECT id FROM productos WHERE id_marca = :marcaId))
                  AND (:categoriaId IS NULL OR t.producto_id IN (
                      SELECT pv.id_producto FROM productos_versiones pv
                      WHERE pv.id_categoria = :categoriaId AND pv.vigencia_hasta IS NULL
                  ))
                  AND (:departamentoId IS NULL OR t.producto_id IN (
                      SELECT pv.id_producto FROM productos_versiones pv
                      JOIN categorias c ON c.id = pv.id_categoria
                      WHERE c.id_departamento = :departamentoId AND pv.vigencia_hasta IS NULL
                  ))
                  AND (:anios IS NULL OR t.anio IN (:anios))
                  AND (:meses IS NULL OR t.mes IN (:meses))
            ) ranked
            JOIN productos p ON p.id = ranked.productoId
            WHERE ranked.rn = 1
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
