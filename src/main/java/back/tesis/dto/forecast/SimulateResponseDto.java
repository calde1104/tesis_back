package back.tesis.dto.forecast;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SimulateResponseDto(
        String status,
        @JsonProperty("id_producto") String idProducto,
        @JsonProperty("variaciones_aplicadas") VariacionesAplicadasDto variacionesAplicadas,
        List<SimulacionMesDto> simulacion) {
}
