package back.tesis.modules.context;

import back.tesis.entities.VariableMacroeconomica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariableMacroeconomicaRepository extends JpaRepository<VariableMacroeconomica, Integer> {
    List<VariableMacroeconomica> findAllByOrderByAnioAscMesAsc();
}
