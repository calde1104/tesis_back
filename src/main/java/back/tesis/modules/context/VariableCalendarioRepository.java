package back.tesis.modules.context;

import back.tesis.entities.VariableCalendario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariableCalendarioRepository extends JpaRepository<VariableCalendario, Integer> {
    List<VariableCalendario> findAllByOrderByAnioAscMesAsc();
}
