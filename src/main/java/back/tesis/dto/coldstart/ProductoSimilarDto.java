package back.tesis.dto.coldstart;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductoSimilarDto(
        @JsonProperty("id_producto") String idProducto,
        String sku,
        String descripcion,
        String departamento,
        String categoria,
        String marca,
        @JsonProperty("similitud_pct") Double similitudPct,
        @JsonProperty("ventas_promedio_mensual") Double ventasPromedioMensual) {
}
