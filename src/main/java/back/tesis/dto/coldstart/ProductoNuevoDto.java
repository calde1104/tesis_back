package back.tesis.dto.coldstart;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductoNuevoDto(
        String descripcion,
        @JsonProperty("cluster_asignado") Integer clusterAsignado,
        @JsonProperty("unidades_base_estimadas") Double unidadesBaseEstimadas,
        @JsonProperty("factor_elasticidad_precio") Double factorElasticidadPrecio) {
}
