package back.tesis.modules.marketshare;

import back.tesis.entities.EmpresaRepository;
import back.tesis.modules.sales.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketShareServiceTest {

    @Mock
    private VentaMensualMarketShareRepository repository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Test
    void getOverviewComputesPesoRealDividiendoUnidadesEmpresaSobreMercado() {
        when(repository.findUnidadesEmpresaPorPeriodo("emp-1", null, null, null, null, null))
                .thenReturn(List.of(periodoRow(2024, 3, 25L)));
        when(repository.findCategoriaIdsDondeCompiteEmpresa("emp-1", null, null)).thenReturn(List.of(10));
        when(repository.findUnidadesMercadoPorPeriodo(List.of(10), null, null, null))
                .thenReturn(List.of(periodoRow(2024, 3, 100L)));

        MarketShareService service = new MarketShareService(repository, categoriaRepository, empresaRepository);

        var result = service.getOverview("emp-1", null, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).anio()).isEqualTo(2024);
        assertThat(result.get(0).marketShareUnidadesPromedio()).isEqualTo(25.0);
    }

    @Test
    void getOverviewReturnsNullPctWhenEmpresaHasNoCategoriasResueltas() {
        when(repository.findUnidadesEmpresaPorPeriodo("emp-1", null, null, null, null, null))
                .thenReturn(List.of(periodoRow(2024, 3, 25L)));
        when(repository.findCategoriaIdsDondeCompiteEmpresa("emp-1", null, null)).thenReturn(List.of());

        MarketShareService service = new MarketShareService(repository, categoriaRepository, empresaRepository);

        var result = service.getOverview("emp-1", null, null, null, null, null);

        assertThat(result.get(0).marketShareUnidadesPromedio()).isNull();
        verify(repository, never()).findUnidadesMercadoPorPeriodo(any(), any(), any(), any());
    }

    @Test
    void getOverviewResolvesCategoriaScopeFromMarcaIdWhenGiven() {
        when(repository.findUnidadesEmpresaPorPeriodo("emp-1", "marca-1", null, 5, List.of(2023, 2024), List.of(1, 2)))
                .thenReturn(List.of());
        when(repository.findCategoriaIdsForMarca("marca-1")).thenReturn(List.of(10, 20));
        when(repository.findUnidadesMercadoPorPeriodo(List.of(10, 20), 5, List.of(2023, 2024), List.of(1, 2)))
                .thenReturn(List.of());

        MarketShareService service = new MarketShareService(repository, categoriaRepository, empresaRepository);

        service.getOverview("emp-1", "marca-1", null, 5, List.of(2023, 2024), List.of(1, 2));

        verify(repository).findCategoriaIdsForMarca("marca-1");
        verify(repository).findUnidadesMercadoPorPeriodo(List.of(10, 20), 5, List.of(2023, 2024), List.of(1, 2));
    }

    @Test
    void getOverviewResolvesCategoriaScopeFromCategoriaIdWhenGivenWithoutMarcaId() {
        when(repository.findUnidadesEmpresaPorPeriodo("emp-1", null, 10, 5, List.of(2023), List.of(1)))
                .thenReturn(List.of());
        when(repository.findUnidadesMercadoPorPeriodo(List.of(10), 5, List.of(2023), List.of(1)))
                .thenReturn(List.of());

        MarketShareService service = new MarketShareService(repository, categoriaRepository, empresaRepository);

        service.getOverview("emp-1", null, 10, 5, List.of(2023), List.of(1));

        verify(repository).findUnidadesMercadoPorPeriodo(List.of(10), 5, List.of(2023), List.of(1));
    }

    @Test
    void getPriceGapPassesFiltersThroughToRepository() {
        when(repository.findUltimoPriceGapPorProducto("emp-1", "marca-1", 10, 5, List.of(2023), List.of(6)))
                .thenReturn(List.of());

        MarketShareService service = new MarketShareService(repository, categoriaRepository, empresaRepository);

        service.getPriceGap("emp-1", "marca-1", 10, 5, List.of(2023), List.of(6));

        verify(repository).findUltimoPriceGapPorProducto("emp-1", "marca-1", 10, 5, List.of(2023), List.of(6));
    }

    @Test
    void getMarketShareUnidadesPromedioPorPeriodoComputesPesoReal() {
        when(repository.sumUnidadesEmpresaPorPeriodo("emp-1", 2024, 3)).thenReturn(25L);
        when(repository.findCategoriaIdsDondeCompiteEmpresaEnPeriodo("emp-1", 2024, 3)).thenReturn(List.of(10));
        when(repository.sumUnidadesMercadoPorPeriodo(2024, 3, List.of(10))).thenReturn(100L);

        MarketShareService service = new MarketShareService(repository, categoriaRepository, empresaRepository);

        assertThat(service.getMarketShareUnidadesPromedioPorPeriodo("emp-1", 2024, 3)).isEqualTo(25.0);
    }

    @Test
    void getMarketShareUnidadesPromedioPorPeriodoReturnsNullWithoutMercadoData() {
        when(repository.sumUnidadesEmpresaPorPeriodo("emp-1", 2024, 3)).thenReturn(25L);
        when(repository.findCategoriaIdsDondeCompiteEmpresaEnPeriodo("emp-1", 2024, 3)).thenReturn(List.of(10));
        when(repository.sumUnidadesMercadoPorPeriodo(2024, 3, List.of(10))).thenReturn(null);

        MarketShareService service = new MarketShareService(repository, categoriaRepository, empresaRepository);

        assertThat(service.getMarketShareUnidadesPromedioPorPeriodo("emp-1", 2024, 3)).isNull();
    }

    @Test
    void getMarketShareUnidadesPromedioPorPeriodoReturnsNullWhenEmpresaHasNoCategoriasResueltas() {
        when(repository.sumUnidadesEmpresaPorPeriodo("emp-1", 2024, 3)).thenReturn(25L);
        when(repository.findCategoriaIdsDondeCompiteEmpresaEnPeriodo("emp-1", 2024, 3)).thenReturn(List.of());

        MarketShareService service = new MarketShareService(repository, categoriaRepository, empresaRepository);

        assertThat(service.getMarketShareUnidadesPromedioPorPeriodo("emp-1", 2024, 3)).isNull();
        verify(repository, never()).sumUnidadesMercadoPorPeriodo(anyInt(), anyInt(), any());
    }

    @Test
    void getPriceGapPctPromedioPorPeriodoDelegatesToRepository() {
        when(repository.findPriceGapPctPromedioPorPeriodo("emp-1", 2024, 3)).thenReturn(10.0);

        MarketShareService service = new MarketShareService(repository, categoriaRepository, empresaRepository);

        assertThat(service.getPriceGapPctPromedioPorPeriodo("emp-1", 2024, 3)).isEqualTo(10.0);
    }

    @Test
    void getCategoriesSummaryComputesCuotaPorCategoriaIndependientemente() {
        when(repository.findCategoriaIdsDondeCompiteEmpresaEnPeriodo("emp-1", 2024, 3, null))
                .thenReturn(List.of(10, 20));
        when(repository.findUnidadesPorCategoriaYEmpresa(2024, 3, List.of(10, 20), null))
                .thenReturn(List.of(
                        categoriaEmpresaRow(10, "emp-1", 40L),
                        categoriaEmpresaRow(10, "emp-2", 60L),
                        categoriaEmpresaRow(20, "emp-1", 30L)));
        when(categoriaRepository.findAllById(List.of(10, 20))).thenReturn(List.of());
        when(empresaRepository.findAllById(List.of("emp-1", "emp-2"))).thenReturn(List.of());

        MarketShareService service = new MarketShareService(repository, categoriaRepository, empresaRepository);

        var result = service.getCategoriesSummary("emp-1", 2024, 3, null);

        assertThat(result).hasSize(2);
        var cat10 = result.stream().filter(r -> r.nuestraCuota() != null && r.nuestraCuota() == 40.0).findFirst().orElseThrow();
        assertThat(cat10.rankingCompetencia()).hasSize(2);
        var cat20 = result.stream().filter(r -> r.nuestraCuota() != null && r.nuestraCuota() == 100.0).findFirst().orElseThrow();
        assertThat(cat20.rankingCompetencia()).hasSize(1);
    }

    @Test
    void getCategoriesSummaryReturnsEmptyWhenEmpresaHasNoCategoriasResueltas() {
        when(repository.findCategoriaIdsDondeCompiteEmpresaEnPeriodo("emp-1", 2024, 3, null))
                .thenReturn(List.of());

        MarketShareService service = new MarketShareService(repository, categoriaRepository, empresaRepository);

        assertThat(service.getCategoriesSummary("emp-1", 2024, 3, null)).isEmpty();
        verify(repository, never()).findUnidadesPorCategoriaYEmpresa(anyInt(), anyInt(), any(), any());
    }

    private static VentaMensualMarketShareRepository.CategoriaEmpresaUnidadesRow categoriaEmpresaRow(
            int categoriaId, String empresaId, long totalUnidades) {
        return new VentaMensualMarketShareRepository.CategoriaEmpresaUnidadesRow() {
            public Integer getCategoriaId() {
                return categoriaId;
            }

            public String getEmpresaId() {
                return empresaId;
            }

            public Long getTotalUnidades() {
                return totalUnidades;
            }
        };
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
