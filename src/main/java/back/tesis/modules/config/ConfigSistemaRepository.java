package back.tesis.modules.config;

import back.tesis.entities.ConfigSistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigSistemaRepository extends JpaRepository<ConfigSistema, Integer> {
    Optional<ConfigSistema> findByEmpresaIdAndParametro(String empresaId, String parametro);
}
