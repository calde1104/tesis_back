package back.tesis.modules.dashboard;

import back.tesis.dto.marketshare.PriceGapDto;
import back.tesis.dto.sales.MonthlyRevenueDto;
import back.tesis.dto.sales.SalesSummaryDto;
import back.tesis.entities.EmpresaRepository;
import back.tesis.modules.context.ContextService;
import back.tesis.modules.marketshare.MarketShareService;
import back.tesis.modules.sales.SalesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private SalesService salesService;
    @Mock
    private MarketShareService marketShareService;
    @Mock
    private MarketShareCompositionRepository compositionRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private ContextService contextService;

    @Test
    void getKpisCombinesSalesAndMarketShareWithNullForecast() {
        when(salesService.getSummary("emp-1", 2024, 3)).thenReturn(new SalesSummaryDto(500L, new BigDecimal("12345.00")));
        when(marketShareService.getOverview("emp-1")).thenReturn(List.of());
        when(marketShareService.getPriceGap("emp-1")).thenReturn(List.of());

        DashboardService service = new DashboardService(
                salesService, marketShareService, compositionRepository, empresaRepository, contextService);

        var result = service.getKpis("emp-1", 2024, 3);

        assertThat(result.ventasUnidades()).isEqualTo(500L);
        assertThat(result.proyeccionUnidadesMes()).isNull();
    }

    @Test
    void getPriceGapPromedioExcludesNullValuesAndIncludesOnlyNonNulls() {
        when(salesService.getSummary("emp-1", 2024, 3)).thenReturn(new SalesSummaryDto(500L, new BigDecimal("12345.00")));
        when(marketShareService.getOverview("emp-1")).thenReturn(List.of());
        when(marketShareService.getPriceGap("emp-1")).thenReturn(List.of(
                new PriceGapDto("prod-1", 10.0, 5.0, 2024, 3),
                new PriceGapDto("prod-2", 20.0, null, 2024, 3),
                new PriceGapDto("prod-3", 30.0, 15.0, 2024, 3)
        ));

        DashboardService service = new DashboardService(
                salesService, marketShareService, compositionRepository, empresaRepository, contextService);

        var result = service.getKpis("emp-1", 2024, 3);

        assertThat(result.priceGapPctPromedio()).isEqualTo(10.0);
    }

    @Test
    void getPriceGapPromedioReturnsNullWhenAllValuesAreNull() {
        when(salesService.getSummary("emp-1", 2024, 3)).thenReturn(new SalesSummaryDto(500L, new BigDecimal("12345.00")));
        when(marketShareService.getOverview("emp-1")).thenReturn(List.of());
        when(marketShareService.getPriceGap("emp-1")).thenReturn(List.of(
                new PriceGapDto("prod-1", 10.0, null, 2024, 3),
                new PriceGapDto("prod-2", 20.0, null, 2024, 3)
        ));

        DashboardService service = new DashboardService(
                salesService, marketShareService, compositionRepository, empresaRepository, contextService);

        var result = service.getKpis("emp-1", 2024, 3);

        assertThat(result.priceGapPctPromedio()).isNull();
    }

    @Test
    void getSalesEvolutionPopulatesMontoFacturadoFromRealSalesAndLeavesForecastNull() {
        when(salesService.getMonthlyRevenue("emp-1")).thenReturn(List.of(
                new MonthlyRevenueDto(2024, 1, new BigDecimal("1000.00")),
                new MonthlyRevenueDto(2024, 2, new BigDecimal("2500.50"))
        ));

        DashboardService service = new DashboardService(
                salesService, marketShareService, compositionRepository, empresaRepository, contextService);

        var result = service.getSalesEvolution("emp-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).anio()).isEqualTo(2024);
        assertThat(result.get(0).mes()).isEqualTo(1);
        assertThat(result.get(0).montoFacturado()).isEqualByComparingTo("1000.00");
        assertThat(result.get(0).unidadesPronosticadas()).isNull();
        assertThat(result.get(1).montoFacturado()).isEqualByComparingTo("2500.50");
        assertThat(result.get(1).unidadesPronosticadas()).isNull();
    }
}
