package back.tesis.modules.sales;

import back.tesis.entities.Marca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarcaFilterRepository extends JpaRepository<Marca, String> {
    List<Marca> findByEmpresaIdOrderByNombre(String empresaId);
}
