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
            """)
    List<String> findActiveProductoIds(
            @Param("empresaId") String empresaId,
            @Param("cutoffAnio") int cutoffAnio,
            @Param("cutoffMes") int cutoffMes
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
}
