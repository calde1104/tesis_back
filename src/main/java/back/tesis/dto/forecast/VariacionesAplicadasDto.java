package back.tesis.dto.forecast;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VariacionesAplicadasDto(
        @JsonProperty("precio_pct") Double precioPct,
        @JsonProperty("usd_pct") Double usdPct,
        @JsonProperty("ipc_pct") Double ipcPct) {
}
