package back.tesis.modules.config;

import back.tesis.entities.ConfigSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConfigSistemaRepository extends JpaRepository<ConfigSistema, Integer> {

    List<ConfigSistema> findByEmpresaIdOrderByParametroAsc(String empresaId);

    @Query("""
            SELECT c FROM ConfigSistema c
            WHERE c.empresaId = :empresaId
              AND c.parametro = :parametro
              AND ((:productoId IS NULL AND c.productoId IS NULL) OR c.productoId = :productoId)
            """)
    Optional<ConfigSistema> findByEmpresaIdAndProductoIdAndParametro(
            @Param("empresaId") String empresaId,
            @Param("productoId") String productoId,
            @Param("parametro") String parametro
    );
}
