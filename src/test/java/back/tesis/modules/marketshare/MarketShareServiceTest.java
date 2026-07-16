package back.tesis.modules.marketshare;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketShareServiceTest {

    @Mock
    private VentaMensualMarketShareRepository repository;

    @Test
    void getOverviewMapsRepositoryRowsToDto() {
        VentaMensualMarketShareRepository.OverviewRow row = new VentaMensualMarketShareRepository.OverviewRow() {
            public int getAnio() {
                return 2024;
            }

            public int getMes() {
                return 3;
            }

            public Double getMarketShareUnidadesPromedio() {
                return 0.42;
            }
        };
        when(repository.findMarketShareEvolucion("emp-1")).thenReturn(List.of(row));

        MarketShareService service = new MarketShareService(repository);

        var result = service.getOverview("emp-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).anio()).isEqualTo(2024);
        assertThat(result.get(0).marketShareUnidadesPromedio()).isEqualTo(0.42);
    }
}
