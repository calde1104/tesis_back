package back.tesis.dto.dashboard;

import java.math.BigDecimal;

public record SalesEvolutionPointDto(
        int anio,
        int mes,
        BigDecimal montoFacturado,
        Long unidadesPronosticadas
) {
}
