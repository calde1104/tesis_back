package back.tesis.modules.ingestion;

import back.tesis.entities.VentaMensual;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaCountRepository extends JpaRepository<VentaMensual, Long> {
    long countByArchivoId(String archivoId);

    boolean existsByProductoIdAndEmpresaIdAndAnioAndMes(String productoId, String empresaId, int anio, int mes);

    boolean existsByProductoIdAndEmpresaIdAndAnioAndMesAndArchivoId(
            String productoId, String empresaId, int anio, int mes, String archivoId);
}
