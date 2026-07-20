package back.tesis.dto.dashboard;

import java.math.BigDecimal;

public record KpisDto(
        int anio,
        int mes,
        long ventasUnidades,
        BigDecimal ventasFacturado,
        Long proyeccionUnidadesMes,
        Double marketShareUnidadesPromedio,
        Double priceGapPctPromedio,
        Double shareOfAssortmentPct
) {
}
