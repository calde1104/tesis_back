package back.tesis.mapper.notificaciones;

import back.tesis.dto.notificaciones.NotificacionDto;
import back.tesis.entities.EventoSistema;

public final class NotificacionMapper {

    private NotificacionMapper() {
    }

    public static NotificacionDto toDto(EventoSistema entity) {
        return new NotificacionDto(
                entity.getId(), entity.getEmpresaId(), entity.getProductoId(), entity.getTipoEvento(),
                entity.getSeveridad(), entity.getPayload(), entity.getEstado(), entity.getFechaGeneracion());
    }
}
