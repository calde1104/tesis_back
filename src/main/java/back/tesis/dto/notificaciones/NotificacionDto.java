package back.tesis.dto.notificaciones;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.time.LocalDateTime;

public record NotificacionDto(
        String id,
        String empresaId,
        String productoId,
        String tipoEvento,
        String severidad,
        @JsonRawValue String payload,
        String estado,
        LocalDateTime fechaGeneracion
) {
}
