package back.tesis.modules.forecast;

import back.tesis.dto.forecast.SimulateRequest;
import back.tesis.dto.forecast.SimulateResponseDto;

public interface MlForecastClient {
    SimulateResponseDto simulate(String empresaId, SimulateRequest request);
}
