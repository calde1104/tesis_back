package back.tesis.modules.notificaciones;

import back.tesis.entities.EventoSistema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventoSistemaRepository extends JpaRepository<EventoSistema, String> {

    Page<EventoSistema> findByEmpresaIdOrderByFechaGeneracionDesc(String empresaId, Pageable pageable);

    Optional<EventoSistema> findByIdAndEmpresaId(String id, String empresaId);

    @Query(value = """
            SELECT JSON_EXTRACT(payload, '$.umbralDemandaMaxima') FROM eventos_sistema
            WHERE id_empresa = :empresaId AND id_producto = :productoId AND tipo_evento = :tipoEvento
              AND JSON_EXTRACT(payload, '$.anio') = :anio AND JSON_EXTRACT(payload, '$.mes') = :mes
            ORDER BY fecha_generacion DESC LIMIT 1
            """, nativeQuery = true)
    Optional<Double> findUmbralDeLaUltimaAlertaDelPeriodo(
            @Param("empresaId") String empresaId,
            @Param("productoId") String productoId,
            @Param("tipoEvento") String tipoEvento,
            @Param("anio") int anio,
            @Param("mes") int mes);
}
