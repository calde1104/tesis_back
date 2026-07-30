package back.tesis.dto.forecast;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SimulateRequest(
        @NotBlank @JsonProperty("id_producto") String idProducto,
        @NotNull @DecimalMin("-10.0") @DecimalMax("10.0") @JsonProperty("precio_pct") Double precioPct,
        @NotNull @DecimalMin("-10.0") @DecimalMax("10.0") @JsonProperty("usd_pct") Double usdPct,
        @NotNull @DecimalMin("-5.0") @DecimalMax("15.0") @JsonProperty("ipc_pct") Double ipcPct) {
}
