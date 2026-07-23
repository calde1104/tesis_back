package back.tesis.modules.dashboard;

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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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
    @Mock
    private AssortmentRepository assortmentRepository;

    @Test
    void getKpisCombinesSalesAndMarketShareWithNullForecast() {
        when(salesService.getSummary("emp-1", 2024, 3)).thenReturn(new SalesSummaryDto(500L, new BigDecimal("12345.00")));

        DashboardService service = new DashboardService(
                salesService, marketShareService, compositionRepository, empresaRepository, contextService, assortmentRepository);

        var result = service.getKpis("emp-1", 2024, 3);

        assertThat(result.ventasUnidades()).isEqualTo(500L);
        assertThat(result.proyeccionUnidadesMes()).isNull();
    }

    @Test
    void getKpisPassesThroughMarketShareAndPriceGapPromediosScopedToRequestedPeriodo() {
        when(salesService.getSummary("emp-1", 2024, 3)).thenReturn(new SalesSummaryDto(500L, new BigDecimal("12345.00")));
        when(marketShareService.getMarketShareUnidadesPromedioPorPeriodo("emp-1", 2024, 3)).thenReturn(0.4);
        when(marketShareService.getPriceGapPctPromedioPorPeriodo("emp-1", 2024, 3)).thenReturn(10.0);

        DashboardService service = new DashboardService(
                salesService, marketShareService, compositionRepository, empresaRepository, contextService, assortmentRepository);

        var result = service.getKpis("emp-1", 2024, 3);

        assertThat(result.marketShareUnidadesPromedio()).isEqualTo(0.4);
        assertThat(result.priceGapPctPromedio()).isEqualTo(10.0);
    }

    @Test
    void getKpisLeavesPriceGapPromedioNullWhenServiceReturnsNull() {
        when(salesService.getSummary("emp-1", 2024, 3)).thenReturn(new SalesSummaryDto(500L, new BigDecimal("12345.00")));
        when(marketShareService.getPriceGapPctPromedioPorPeriodo("emp-1", 2024, 3)).thenReturn(null);

        DashboardService service = new DashboardService(
                salesService, marketShareService, compositionRepository, empresaRepository, contextService, assortmentRepository);

        var result = service.getKpis("emp-1", 2024, 3);

        assertThat(result.priceGapPctPromedio()).isNull();
    }

    @Test
    void getKpisCalculatesShareOfAssortmentFromActiveProductsAndMarketTotal() {
        when(salesService.getSummary("emp-1", 2024, 3)).thenReturn(new SalesSummaryDto(500L, new BigDecimal("12345.00")));
        when(assortmentRepository.findActiveProductoIds(eq("emp-1"), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of("prod-1", "prod-2"));
        when(assortmentRepository.findCategoriaIdsForProductos(List.of("prod-1", "prod-2")))
                .thenReturn(List.of(10, 20));
        when(assortmentRepository.countMarketProductosByCategorias(List.of(10, 20))).thenReturn(8L);

        DashboardService service = new DashboardService(
                salesService, marketShareService, compositionRepository, empresaRepository, contextService, assortmentRepository);

        var result = service.getKpis("emp-1", 2024, 3);

        assertThat(result.shareOfAssortmentPct()).isEqualTo(25.0);
    }

    @Test
    void getKpisReturnsNullShareOfAssortmentWhenEmpresaHasNoActiveProducts() {
        when(salesService.getSummary("emp-1", 2024, 3)).thenReturn(new SalesSummaryDto(0L, BigDecimal.ZERO));
        when(assortmentRepository.findActiveProductoIds(eq("emp-1"), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(List.of());

        DashboardService service = new DashboardService(
                salesService, marketShareService, compositionRepository, empresaRepository, contextService, assortmentRepository);

        var result = service.getKpis("emp-1", 2024, 3);

        assertThat(result.shareOfAssortmentPct()).isNull();
    }

    @Test
    void getSalesEvolutionPopulatesMontoFacturadoAndUnidadesVendidasFromRealSalesAndLeavesForecastNull() {
        when(salesService.getMonthlyRevenue("emp-1")).thenReturn(List.of(
                new MonthlyRevenueDto(2024, 1, new BigDecimal("1000.00"), 50L),
                new MonthlyRevenueDto(2024, 2, new BigDecimal("2500.50"), 120L)
        ));

        DashboardService service = new DashboardService(
                salesService, marketShareService, compositionRepository, empresaRepository, contextService, assortmentRepository);

        var result = service.getSalesEvolution("emp-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).anio()).isEqualTo(2024);
        assertThat(result.get(0).mes()).isEqualTo(1);
        assertThat(result.get(0).montoFacturado()).isEqualByComparingTo("1000.00");
        assertThat(result.get(0).unidadesVendidas()).isEqualTo(50L);
        assertThat(result.get(0).unidadesPronosticadas()).isNull();
        assertThat(result.get(1).montoFacturado()).isEqualByComparingTo("2500.50");
        assertThat(result.get(1).unidadesVendidas()).isEqualTo(120L);
        assertThat(result.get(1).unidadesPronosticadas()).isNull();
    }

    @Test
    void getMarketShareCompositionPassesEmpresaIdToRepositoryToScopeByCategorias() {
        MarketShareCompositionRepository.CompositionRow row = new MarketShareCompositionRepository.CompositionRow() {
            public String getEmpresaId() { return "emp-1"; }
            public Double getMarketShareUnidadesPromedio() { return 42.0; }
        };
        when(compositionRepository.findComposicionPorPeriodo("emp-1", 2024, 3)).thenReturn(List.of(row));
        when(empresaRepository.findAllById(List.of("emp-1"))).thenReturn(List.of());

        DashboardService service = new DashboardService(
                salesService, marketShareService, compositionRepository, empresaRepository, contextService, assortmentRepository);

        var result = service.getMarketShareComposition("emp-1", 2024, 3);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).empresaId()).isEqualTo("emp-1");
        assertThat(result.get(0).marketShareUnidadesPromedio()).isEqualTo(42.0);
    }
}
