package back.tesis.dto.forecast;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VariableImportanciaDto(
        String feature,
        @JsonProperty("nombre_descriptivo") String nombreDescriptivo,
        String grupo,
        @JsonProperty("importancia_pct") Double importanciaPct,
        @JsonProperty("nivel_impacto") String nivelImpacto) {
}
