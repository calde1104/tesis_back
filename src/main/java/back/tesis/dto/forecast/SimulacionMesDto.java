package back.tesis.dto.forecast;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SimulacionMesDto(
        int anio,
        int mes,
        @JsonProperty("unidades_simuladas") Double unidadesSimuladas,
        @JsonProperty("monto_simulado") Double montoSimulado,
        @JsonProperty("precio_unitario_simulado") Double precioUnitarioSimulado) {
}
