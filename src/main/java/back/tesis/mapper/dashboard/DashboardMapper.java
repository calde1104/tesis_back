package back.tesis.mapper.dashboard;

import back.tesis.dto.context.CalendarioDto;
import back.tesis.dto.context.MacroDto;
import back.tesis.dto.dashboard.ContextVariablesDto;
import back.tesis.dto.dashboard.KpisDto;
import back.tesis.dto.dashboard.MarketShareCompositionDto;
import back.tesis.dto.dashboard.SalesEvolutionPointDto;
import back.tesis.dto.sales.MonthlyRevenueDto;
import back.tesis.dto.sales.SalesSummaryDto;
import back.tesis.modules.dashboard.MarketShareCompositionRepository;

public final class DashboardMapper {

    private DashboardMapper() {
    }

    public static KpisDto toKpisDto(int anio, int mes, SalesSummaryDto summary, Double marketShareProm, Double priceGapProm) {
        return new KpisDto(anio, mes, summary.totalUnidades(), summary.totalFacturado(), null, marketShareProm, priceGapProm);
    }

    public static SalesEvolutionPointDto toSalesEvolutionDto(MonthlyRevenueDto point) {
        return new SalesEvolutionPointDto(point.anio(), point.mes(), point.montoFacturado(), null);
    }

    public static MarketShareCompositionDto toCompositionDto(MarketShareCompositionRepository.CompositionRow row, String empresaNombre) {
        return new MarketShareCompositionDto(row.getEmpresaId(), empresaNombre, row.getMarketShareUnidadesPromedio());
    }

    public static ContextVariablesDto toContextVariablesDto(MacroDto macro, CalendarioDto calendario) {
        return new ContextVariablesDto(macro, calendario);
    }
}
