package back.tesis.dto.coldstart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ColdStartResponse(
        String status,
        @JsonProperty("producto_nuevo") ProductoNuevoDto productoNuevo,
        @JsonProperty("productos_similares_gemelos") List<ProductoSimilarDto> productosSimilaresGemelos,
        @JsonProperty("proyeccion_12_meses") List<ProyeccionMesDto> proyeccion12Meses) {
}
