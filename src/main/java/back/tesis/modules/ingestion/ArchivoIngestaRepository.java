package back.tesis.modules.ingestion;

import back.tesis.entities.ArchivoIngesta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArchivoIngestaRepository extends JpaRepository<ArchivoIngesta, String> {
    Page<ArchivoIngesta> findByEmpresaIdOrderByFechaRecepcionDesc(String empresaId, Pageable pageable);

    Optional<ArchivoIngesta> findByIdAndEmpresaId(String id, String empresaId);

    boolean existsByHashSha256AndEstadoNot(String hashSha256, String estado);
}
