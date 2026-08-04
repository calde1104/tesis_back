package back.tesis.dto.marketshare;

import java.util.List;

public record CategoriaMarketShareSummaryDto(
        String categoria,
        Double nuestraCuota,
        List<EmpresaShareDto> rankingCompetencia
) {
}
