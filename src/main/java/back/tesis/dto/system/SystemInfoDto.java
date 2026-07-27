package back.tesis.dto.system;

import java.time.LocalDateTime;

public record SystemInfoDto(
        String razonSocial,
        String cuit,
        String tenantUuid,
        String rolUsuario,
        String emailUsuario,

        String frameworkBase,
        String versionModelo,
        Float mape,
        Float mae,
        Float rmse,
        String rangoEntrenamiento,
        LocalDateTime ultimoReentrenamiento,
        String proximoCron,

        String dbEngine,
        String dbStatus,
        Integer dbLatencyMs
) {
}
