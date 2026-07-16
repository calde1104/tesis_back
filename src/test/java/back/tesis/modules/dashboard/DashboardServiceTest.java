package back.tesis.modules.dashboard;

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
}
