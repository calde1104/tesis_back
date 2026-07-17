package back.tesis.modules.forecast;

import back.tesis.dto.forecast.SimulateRequest;
import back.tesis.dto.forecast.SimulateResponseDto;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StubMlForecastClient implements MlForecastClient {

    @Override
    public SimulateResponseDto simulate(String empresaId, SimulateRequest request) {
        return new SimulateResponseDto(true, Map.of(
                "mensaje", "Microservicio ML no disponible todavia; resultado simulado",
                "parametrosRecibidos", request.parametros()
        ));
    }
}
