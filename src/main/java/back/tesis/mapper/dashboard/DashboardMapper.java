package back.tesis.mapper.dashboard;

import back.tesis.dto.context.CalendarioDto;
import back.tesis.dto.context.MacroDto;
import back.tesis.dto.dashboard.ContextVariablesDto;
import back.tesis.dto.dashboard.KpisDto;
import back.tesis.dto.dashboard.MarketShareCompositionDto;
import back.tesis.dto.dashboard.SalesEvolutionPointDto;
import back.tesis.dto.sales.MonthlyRevenueDto;
import back.tesis.dto.sales.SalesSummaryDto;

public final class DashboardMapper {

    private DashboardMapper() {
    }

    public static KpisDto toKpisDto(
            int anio, int mes, SalesSummaryDto summary,
            Double marketShareProm, Double priceGapProm, Double shareOfAssortmentPct
    ) {
        return new KpisDto(anio, mes, summary.totalUnidades(), summary.totalFacturado(),
                null, marketShareProm, priceGapProm, shareOfAssortmentPct);
    }

    public static SalesEvolutionPointDto toSalesEvolutionDto(MonthlyRevenueDto point) {
        return new SalesEvolutionPointDto(point.anio(), point.mes(), point.montoFacturado(), null, point.unidadesVendidas());
    }

    public static MarketShareCompositionDto toCompositionDto(String empresaId, String empresaNombre, Double marketShareUnidadesPct) {
        return new MarketShareCompositionDto(empresaId, empresaNombre, marketShareUnidadesPct);
    }

    public static ContextVariablesDto toContextVariablesDto(MacroDto macro, CalendarioDto calendario) {
        return new ContextVariablesDto(macro, calendario);
    }
}
