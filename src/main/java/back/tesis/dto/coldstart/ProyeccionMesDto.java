package back.tesis.dto.coldstart;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProyeccionMesDto(
        Integer anio,
        Integer mes,
        @JsonProperty("unidades_predichas") Double unidadesPredichas,
        @JsonProperty("monto_predicho") Double montoPredicho,
        @JsonProperty("intervalo_confianza_inf") Double intervaloConfianzaInf,
        @JsonProperty("intervalo_confianza_sup") Double intervaloConfianzaSup,
        @JsonProperty("factor_estacional_aplicado") Double factorEstacionalAplicado) {
}
