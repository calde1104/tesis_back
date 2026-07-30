package back.tesis.modules.marketshare;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketShareServiceTest {

    @Mock
    private VentaMensualMarketShareRepository repository;

    @Test
    void getOverviewComputesPesoRealDividiendoUnidadesEmpresaSobreMercado() {
        when(repository.findUnidadesEmpresaPorPeriodo("emp-1", null, null, null, null, null))
                .thenReturn(List.of(periodoRow(2024, 3, 25L)));
        when(repository.findUnidadesMercadoPorPeriodo(null, null, null, null, null))
                .thenReturn(List.of(periodoRow(2024, 3, 100L)));

        MarketShareService service = new MarketShareService(repository);

        var result = service.getOverview("emp-1", null, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).anio()).isEqualTo(2024);
        assertThat(result.get(0).marketShareUnidadesPromedio()).isEqualTo(25.0);
    }

    @Test
    void getOverviewReturnsNullPctWhenMercadoDataMissingForThatPeriodo() {
        when(repository.findUnidadesEmpresaPorPeriodo("emp-1", null, null, null, null, null))
                .thenReturn(List.of(periodoRow(2024, 3, 25L)));
        when(repository.findUnidadesMercadoPorPeriodo(null, null, null, null, null))
                .thenReturn(List.of());

        MarketShareService service = new MarketShareService(repository);

        var result = service.getOverview("emp-1", null, null, null, null, null);

        assertThat(result.get(0).marketShareUnidadesPromedio()).isNull();
    }

    @Test
    void getOverviewPassesFiltersThroughToRepository() {
        when(repository.findUnidadesEmpresaPorPeriodo("emp-1", "marca-1", 10, 5, List.of(2023, 2024), List.of(1, 2)))
                .thenReturn(List.of());
        when(repository.findUnidadesMercadoPorPeriodo("marca-1", 10, 5, List.of(2023, 2024), List.of(1, 2)))
                .thenReturn(List.of());

        MarketShareService service = new MarketShareService(repository);

        service.getOverview("emp-1", "marca-1", 10, 5, List.of(2023, 2024), List.of(1, 2));

        verify(repository).findUnidadesEmpresaPorPeriodo("emp-1", "marca-1", 10, 5, List.of(2023, 2024), List.of(1, 2));
        verify(repository).findUnidadesMercadoPorPeriodo("marca-1", 10, 5, List.of(2023, 2024), List.of(1, 2));
    }

    @Test
    void getPriceGapPassesFiltersThroughToRepository() {
        when(repository.findUltimoPriceGapPorProducto("emp-1", "marca-1", 10, 5, List.of(2023), List.of(6)))
                .thenReturn(List.of());

        MarketShareService service = new MarketShareService(repository);

        service.getPriceGap("emp-1", "marca-1", 10, 5, List.of(2023), List.of(6));

        verify(repository).findUltimoPriceGapPorProducto("emp-1", "marca-1", 10, 5, List.of(2023), List.of(6));
    }

    @Test
    void getMarketShareUnidadesPromedioPorPeriodoComputesPesoReal() {
        when(repository.sumUnidadesEmpresaPorPeriodo("emp-1", 2024, 3)).thenReturn(25L);
        when(repository.sumUnidadesMercadoPorPeriodo(2024, 3)).thenReturn(100L);

        MarketShareService service = new MarketShareService(repository);

        assertThat(service.getMarketShareUnidadesPromedioPorPeriodo("emp-1", 2024, 3)).isEqualTo(25.0);
    }

    @Test
    void getMarketShareUnidadesPromedioPorPeriodoReturnsNullWithoutMercadoData() {
        when(repository.sumUnidadesEmpresaPorPeriodo("emp-1", 2024, 3)).thenReturn(25L);
        when(repository.sumUnidadesMercadoPorPeriodo(2024, 3)).thenReturn(null);

        MarketShareService service = new MarketShareService(repository);

        assertThat(service.getMarketShareUnidadesPromedioPorPeriodo("emp-1", 2024, 3)).isNull();
    }

    @Test
    void getPriceGapPctPromedioPorPeriodoDelegatesToRepository() {
        when(repository.findPriceGapPctPromedioPorPeriodo("emp-1", 2024, 3)).thenReturn(10.0);

        MarketShareService service = new MarketShareService(repository);

        assertThat(service.getPriceGapPctPromedioPorPeriodo("emp-1", 2024, 3)).isEqualTo(10.0);
    }

    private static VentaMensualMarketShareRepository.PeriodoUnidadesRow periodoRow(int anio, int mes, long totalUnidades) {
        return new VentaMensualMarketShareRepository.PeriodoUnidadesRow() {
            public int getAnio() {
                return anio;
            }

            public int getMes() {
                return mes;
            }

            public Long getTotalUnidades() {
                return totalUnidades;
            }
        };
    }
}
