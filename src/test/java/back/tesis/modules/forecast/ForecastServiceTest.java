package back.tesis.modules.forecast;

import back.tesis.dto.forecast.SimulateRequest;
import back.tesis.dto.forecast.SimulateResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    @Mock
    private MlForecastClient mlForecastClient;

    @Test
    void getForecastReturnsTwelveMonthsHorizon() {
        ForecastService service = new ForecastService(mlForecastClient);

        var result = service.getForecast("emp-1");

        assertThat(result).hasSize(12);
    }

    @Test
    void simulateDelegatesToMlClientSynchronously() {
        SimulateRequest request = new SimulateRequest(Map.of("descuento", 10));
        when(mlForecastClient.simulate("emp-1", request))
                .thenReturn(new SimulateResponseDto(true, Map.of("ok", true)));

        ForecastService service = new ForecastService(mlForecastClient);

        var result = service.simulate("emp-1", request);

        assertThat(result.simulado()).isTrue();
    }
}
