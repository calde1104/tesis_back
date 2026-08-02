package back.tesis.modules.dashboard;

import back.tesis.dto.dashboard.MarcaShareOfAssortmentDto;
import back.tesis.dto.dashboard.ShareOfAssortmentCompositionDto;
import back.tesis.dto.sales.MonthlyRevenueDto;
import back.tesis.dto.sales.SalesSummaryDto;
import back.tesis.entities.EmpresaRepository;
import back.tesis.entities.Marca;
import back.tesis.modules.context.ContextService;
import back.tesis.modules.marketshare.MarketShareService;
import back.tesis.modules.sales.MarcaFilterRepository;
import back.tesis.modules.sales.SalesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    @Mock
    private MarcaFilterRepository marcaRepository;

    private DashboardService newService() {
        return new DashboardService(
                salesService, marketShareService, compositionRepository, empresaRepository, contextService,
                assortmentRepository, marcaRepository);
    }

    @Test
    void getKpisCombinesSalesAndMarketShareWithNullForecast() {
        when(salesService.getSummary("emp-1", 2024, 3)).thenReturn(new SalesSummaryDto(500L, new BigDecimal("12345.00")));

        var result = newService().getKpis("emp-1", 2024, 3);

        assertThat(result.ventasUnidades()).isEqualTo(500L);
        assertThat(result.proyeccionUnidadesMes()).isNull();
    }

    @Test
    void getKpisPassesThroughMarketShareAndPriceGapPromediosScopedToRequestedPeriodo() {
        when(salesService.getSummary("emp-1", 2024, 3)).thenReturn(new SalesSummaryDto(500L, new BigDecimal("12345.00")));
        when(marketShareService.getMarketShareUnidadesPromedioPorPeriodo("emp-1", 2024, 3)).thenReturn(0.4);
        when(marketShareService.getPriceGapPctPromedioPorPeriodo("emp-1", 2024, 3)).thenReturn(10.0);

        var result = newService().getKpis("emp-1", 2024, 3);

        assertThat(result.marketShareUnidadesPromedio()).isEqualTo(0.4);
        assertThat(result.priceGapPctPromedio()).isEqualTo(10.0);
    }

    @Test
    void getKpisLeavesPriceGapPromedioNullWhenServiceReturnsNull() {
        when(salesService.getSummary("emp-1", 2024, 3)).thenReturn(new SalesSummaryDto(500L, new BigDecimal("12345.00")));
        when(marketShareService.getPriceGapPctPromedioPorPeriodo("emp-1", 2024, 3)).thenReturn(null);

        var result = newService().getKpis("emp-1", 2024, 3);

        assertThat(result.priceGapPctPromedio()).isNull();
    }

    @Test
    void getKpisCalculatesShareOfAssortmentFromActiveProductsAndMarketTotal() {
        when(salesService.getSummary("emp-1", 2024, 3)).thenReturn(new SalesSummaryDto(500L, new BigDecimal("12345.00")));
        when(assortmentRepository.findActiveProductoIds(
                eq("emp-1"), anyInt(), anyInt(), anyInt(), anyInt(), isNull(), isNull(), isNull()))
                .thenReturn(List.of("prod-1", "prod-2"));
        when(assortmentRepository.findCategoriaIdsForProductos(List.of("prod-1", "prod-2")))
                .thenReturn(List.of(10, 20));
        when(assortmentRepository.countMarketProductosByCategorias(List.of(10, 20))).thenReturn(8L);

        var result = newService().getKpis("emp-1", 2024, 3);

        assertThat(result.shareOfAssortmentPct()).isEqualTo(25.0);
    }

    @Test
    void getKpisReturnsNullShareOfAssortmentWhenEmpresaHasNoActiveProducts() {
        when(salesService.getSummary("emp-1", 2024, 3)).thenReturn(new SalesSummaryDto(0L, BigDecimal.ZERO));
        when(assortmentRepository.findActiveProductoIds(
                eq("emp-1"), anyInt(), anyInt(), anyInt(), anyInt(), isNull(), isNull(), isNull())).thenReturn(List.of());

        var result = newService().getKpis("emp-1", 2024, 3);

        assertThat(result.shareOfAssortmentPct()).isNull();
    }

    @Test
    void getSalesEvolutionPopulatesMontoFacturadoAndUnidadesVendidasFromRealSalesAndLeavesForecastNull() {
        when(salesService.getMonthlyRevenue("emp-1")).thenReturn(List.of(
                new MonthlyRevenueDto(2024, 1, new BigDecimal("1000.00"), 50L),
                new MonthlyRevenueDto(2024, 2, new BigDecimal("2500.50"), 120L)
        ));

        var result = newService().getSalesEvolution("emp-1");

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
        MarketShareCompositionRepository.CompositionRow row1 = compositionRow("emp-1", 25L);
        MarketShareCompositionRepository.CompositionRow row2 = compositionRow("emp-2", 75L);
        when(compositionRepository.findCategoriaIdsDondeCompiteEmpresa("emp-1", 2024, 3)).thenReturn(List.of(10));
        when(compositionRepository.findComposicionPorPeriodo(2024, 3, List.of(10), null, null, null))
                .thenReturn(List.of(row1, row2));
        when(empresaRepository.findAllById(List.of("emp-1", "emp-2"))).thenReturn(List.of());

        var result = newService().getMarketShareComposition("emp-1", 2024, 3, null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).empresaId()).isEqualTo("emp-1");
        assertThat(result.get(0).marketShareUnidadesPromedio()).isEqualTo(25.0);
        assertThat(result.get(1).marketShareUnidadesPromedio()).isEqualTo(75.0);
    }

    @Test
    void getMarketShareCompositionReturnsEmptyWhenEmpresaHasNoCategoriasResueltas() {
        when(compositionRepository.findCategoriaIdsDondeCompiteEmpresa("emp-1", 2024, 3)).thenReturn(List.of());

        var result = newService().getMarketShareComposition("emp-1", 2024, 3, null, null, null);

        assertThat(result).isEmpty();
        verify(compositionRepository, never()).findComposicionPorPeriodo(
                anyInt(), anyInt(), any(), any(), any(), any());
    }

    private static MarketShareCompositionRepository.CompositionRow compositionRow(String empresaId, long totalUnidades) {
        return new MarketShareCompositionRepository.CompositionRow() {
            public String getEmpresaId() { return empresaId; }
            public Long getTotalUnidades() { return totalUnidades; }
        };
    }

    @Test
    void getMarketShareCompositionPassesFiltersThroughToRepository() {
        when(compositionRepository.findCategoriaIdsDondeCompiteEmpresa("emp-1", 2024, 3)).thenReturn(List.of(10));
        when(compositionRepository.findComposicionPorPeriodo(2024, 3, List.of(10), "marca-1", 10, 5))
                .thenReturn(List.of());
        when(empresaRepository.findAllById(List.of())).thenReturn(List.of());

        newService().getMarketShareComposition("emp-1", 2024, 3, "marca-1", 10, 5);

        verify(compositionRepository).findComposicionPorPeriodo(2024, 3, List.of(10), "marca-1", 10, 5);
    }

    @Test
    void getShareOfAssortmentRankingWithoutFiltersReturnsEmpresaRanking() {
        when(assortmentRepository.findActiveProductoIds(
                eq("emp-1"), anyInt(), anyInt(), anyInt(), anyInt(), isNull(), isNull(), eq(5)))
                .thenReturn(List.of("prod-1"));
        when(assortmentRepository.findCategoriaIdsForProductos(List.of("prod-1"))).thenReturn(List.of(10));
        when(assortmentRepository.countMarketProductosByCategorias(List.of(10))).thenReturn(4L);
        when(assortmentRepository.countActiveProductosPorEmpresa(
                anyInt(), anyInt(), anyInt(), anyInt(), eq(List.of(10)), isNull(), eq(5)))
                .thenReturn(List.of(empresaProductoCountRow("emp-1", 1L), empresaProductoCountRow("emp-2", 3L)));
        when(empresaRepository.findAllById(List.of("emp-1", "emp-2"))).thenReturn(List.of());

        var result = newService().getShareOfAssortmentRanking("emp-1", 2024, 3, null, null, 5, null);

        assertThat(result).hasSize(2);
        List<ShareOfAssortmentCompositionDto> ranking = (List<ShareOfAssortmentCompositionDto>) result;
        assertThat(ranking.get(0).empresaId()).isEqualTo("emp-2");
        assertThat(ranking.get(0).shareOfAssortmentPct()).isEqualTo(75.0);
        assertThat(ranking.get(1).empresaId()).isEqualTo("emp-1");
        assertThat(ranking.get(1).shareOfAssortmentPct()).isEqualTo(25.0);
    }

    @Test
    void getShareOfAssortmentRankingReturnsEmptyWhenEmpresaHasNoActiveProducts() {
        when(assortmentRepository.findActiveProductoIds(
                eq("emp-1"), anyInt(), anyInt(), anyInt(), anyInt(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        var result = newService().getShareOfAssortmentRanking("emp-1", 2024, 3, null, null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void getShareOfAssortmentRankingWithAnioDesdeUsesJanuaryOfThatYearAsCutoff() {
        when(assortmentRepository.findActiveProductoIds(
                eq("emp-1"), eq(2024), eq(1), anyInt(), anyInt(), isNull(), isNull(), isNull()))
                .thenReturn(List.of("prod-1"));
        when(assortmentRepository.findCategoriaIdsForProductos(List.of("prod-1"))).thenReturn(List.of(10));
        when(assortmentRepository.countMarketProductosByCategorias(List.of(10))).thenReturn(4L);
        when(assortmentRepository.countActiveProductosPorEmpresa(
                eq(2024), eq(1), anyInt(), anyInt(), eq(List.of(10)), isNull(), isNull()))
                .thenReturn(List.of(empresaProductoCountRow("emp-1", 1L)));
        when(empresaRepository.findAllById(List.of("emp-1"))).thenReturn(List.of());

        var result = newService().getShareOfAssortmentRanking("emp-1", 2025, 6, null, null, null, 2024);

        assertThat(result).hasSize(1);
    }

    @Test
    void getShareOfAssortmentRankingWithCategoriaIdReturnsMarcaRankingScopedToThatCategoria() {
        when(assortmentRepository.countMarketProductosByCategorias(List.of(10))).thenReturn(4L);
        when(assortmentRepository.countActiveProductosPorMarca(anyInt(), anyInt(), anyInt(), anyInt(), eq(List.of(10)), isNull()))
                .thenReturn(List.of(marcaProductoCountRow("marca-1", 1L), marcaProductoCountRow("marca-2", 3L)));
        when(marcaRepository.findAllById(List.of("marca-1", "marca-2"))).thenReturn(List.of());

        var result = newService().getShareOfAssortmentRanking("emp-1", 2024, 3, null, 10, null, null);

        assertThat(result).hasSize(2);
        List<MarcaShareOfAssortmentDto> ranking = (List<MarcaShareOfAssortmentDto>) result;
        assertThat(ranking.get(0).marcaId()).isEqualTo("marca-2");
        assertThat(ranking.get(0).shareOfAssortmentPct()).isEqualTo(75.0);
        assertThat(ranking.get(1).marcaId()).isEqualTo("marca-1");
        assertThat(ranking.get(1).shareOfAssortmentPct()).isEqualTo(25.0);
    }

    @Test
    void getShareOfAssortmentRankingWithMarcaIdResolvesCategoriaFromThatMarca() {
        when(assortmentRepository.findCategoriaIdsForMarca("marca-1")).thenReturn(List.of(10));
        when(assortmentRepository.countMarketProductosByCategorias(List.of(10))).thenReturn(4L);
        when(assortmentRepository.countActiveProductosPorMarca(anyInt(), anyInt(), anyInt(), anyInt(), eq(List.of(10)), isNull()))
                .thenReturn(List.of(marcaProductoCountRow("marca-1", 1L)));
        when(marcaRepository.findAllById(List.of("marca-1"))).thenReturn(List.of(marca("marca-1", "Natura")));

        var result = newService().getShareOfAssortmentRanking("emp-1", 2024, 3, "marca-1", null, null, null);

        assertThat(result).hasSize(1);
        MarcaShareOfAssortmentDto dto = (MarcaShareOfAssortmentDto) result.get(0);
        assertThat(dto.marcaId()).isEqualTo("marca-1");
        assertThat(dto.marcaNombre()).isEqualTo("Natura");
        assertThat(dto.shareOfAssortmentPct()).isEqualTo(25.0);
    }

    @Test
    void getShareOfAssortmentRankingWithCategoriaIdAndAnioDesdeUsesJanuaryOfThatYearAsCutoff() {
        when(assortmentRepository.countMarketProductosByCategorias(List.of(10))).thenReturn(4L);
        when(assortmentRepository.countActiveProductosPorMarca(eq(2024), eq(1), anyInt(), anyInt(), eq(List.of(10)), isNull()))
                .thenReturn(List.of(marcaProductoCountRow("marca-1", 1L)));
        when(marcaRepository.findAllById(List.of("marca-1"))).thenReturn(List.of());

        var result = newService().getShareOfAssortmentRanking("emp-1", 2025, 6, null, 10, null, 2024);

        assertThat(result).hasSize(1);
    }

    private static AssortmentRepository.EmpresaProductoCountRow empresaProductoCountRow(String empresaId, long totalProductos) {
        return new AssortmentRepository.EmpresaProductoCountRow() {
            public String getEmpresaId() { return empresaId; }
            public Long getTotalProductos() { return totalProductos; }
        };
    }

    private static AssortmentRepository.MarcaProductoCountRow marcaProductoCountRow(String marcaId, long totalProductos) {
        return new AssortmentRepository.MarcaProductoCountRow() {
            public String getMarcaId() { return marcaId; }
            public Long getTotalProductos() { return totalProductos; }
        };
    }

    private static Marca marca(String id, String nombre) {
        try {
            var constructor = Marca.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Marca marca = constructor.newInstance();
            var idField = marca.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(marca, id);
            var nombreField = marca.getClass().getDeclaredField("nombre");
            nombreField.setAccessible(true);
            nombreField.set(marca, nombre);
            return marca;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
