package back.tesis.modules.ingestion;

import back.tesis.entities.AuditoriaIntegridad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaIntegridadRepository extends JpaRepository<AuditoriaIntegridad, String> {
}
