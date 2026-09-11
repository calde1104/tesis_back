package back.tesis.dto.forecast;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record ModelExplainabilityDto(
        String status,
        @JsonProperty("modelo_tipo") String modeloTipo,
        @JsonProperty("total_variables") Integer totalVariables,
        @JsonProperty("resumen_por_grupo") Map<String, Double> resumenPorGrupo,
        @JsonProperty("importancia_variables") List<VariableImportanciaDto> importanciaVariables) {
}
