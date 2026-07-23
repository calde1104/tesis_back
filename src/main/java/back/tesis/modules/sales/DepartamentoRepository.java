package back.tesis.modules.sales;

import back.tesis.entities.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DepartamentoRepository extends JpaRepository<Departamento, Integer> {

    @Query("""
            SELECT DISTINCT pv.categoria.departamento
            FROM ProductoVersion pv
            JOIN Marca m ON m.id = pv.producto.marcaId
            WHERE m.empresaId = :empresaId AND pv.vigenciaHasta IS NULL
            ORDER BY pv.categoria.departamento.nombre
            """)
    List<Departamento> findByEmpresaIdOrderByNombre(@Param("empresaId") String empresaId);
}
