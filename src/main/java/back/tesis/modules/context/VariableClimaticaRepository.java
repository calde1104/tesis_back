package back.tesis.modules.context;

import back.tesis.entities.VariableClimatica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariableClimaticaRepository extends JpaRepository<VariableClimatica, Integer> {
    List<VariableClimatica> findAllByOrderByAnioAscMesAsc();
}
