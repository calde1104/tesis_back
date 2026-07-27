package back.tesis.modules.models;

import back.tesis.entities.ModeloVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModeloVersionRepository extends JpaRepository<ModeloVersion, String> {
    List<ModeloVersion> findByEmpresaIdOrderByFechaEntrenamientoDesc(String empresaId);

    Optional<ModeloVersion> findByEmpresaIdAndActivoTrue(String empresaId);
}
