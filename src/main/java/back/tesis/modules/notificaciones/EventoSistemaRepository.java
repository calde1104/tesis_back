package back.tesis.modules.notificaciones;

import back.tesis.entities.EventoSistema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoSistemaRepository extends JpaRepository<EventoSistema, String> {

    Page<EventoSistema> findByEmpresaIdOrderByFechaGeneracionDesc(String empresaId, Pageable pageable);

    boolean existsByEmpresaIdAndProductoIdAndTipoEventoAndEstado(
            String empresaId, String productoId, String tipoEvento, String estado);
}
