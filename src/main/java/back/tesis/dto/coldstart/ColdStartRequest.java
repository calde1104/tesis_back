package back.tesis.dto.coldstart;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ColdStartRequest(
        @NotNull @JsonProperty("id_departamento") Integer idDepartamento,
        @NotNull @JsonProperty("id_categoria") Integer idCategoria,
        @NotBlank @JsonProperty("id_marca") String idMarca,
        @JsonProperty("descripcion") String descripcion,
        @NotNull @Positive @JsonProperty("contenido_neto") Double contenidoNeto,
        @NotBlank @JsonProperty("unidad_medida") String unidadMedida,
        @NotNull @Positive @JsonProperty("valor_unitario") Double valorUnitario,
        @JsonProperty("horizonte_meses") Integer horizonteMeses) {
}
