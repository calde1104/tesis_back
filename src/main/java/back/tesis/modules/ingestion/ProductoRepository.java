package back.tesis.modules.ingestion;

import back.tesis.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, String> {
    List<Producto> findBySku(String sku);
}
