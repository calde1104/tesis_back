package back.tesis.modules.ingestion;

import back.tesis.entities.VentaSemanal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaSemanalCountRepository extends JpaRepository<VentaSemanal, Long> {
    long countByArchivoId(String archivoId);

    long deleteByArchivoId(String archivoId);

    boolean existsByProductoIdAndEmpresaIdAndAnioAndSemana(String productoId, String empresaId, int anio, int semana);

    boolean existsByProductoIdAndEmpresaIdAndAnioAndSemanaAndArchivoId(
            String productoId, String empresaId, int anio, int semana, String archivoId);
}
