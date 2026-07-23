package back.tesis.modules.sales;

import back.tesis.entities.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    @Query("""
            SELECT DISTINCT pv.categoria
            FROM ProductoVersion pv
            JOIN Marca m ON m.id = pv.producto.marcaId
            WHERE m.empresaId = :empresaId AND pv.vigenciaHasta IS NULL
            ORDER BY pv.categoria.nombre
            """)
    List<Categoria> findByEmpresaIdOrderByNombre(@Param("empresaId") String empresaId);
}
