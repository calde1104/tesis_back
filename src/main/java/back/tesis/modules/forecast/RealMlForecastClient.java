package back.tesis.modules.forecast;

import back.tesis.dto.forecast.SimulateRequest;
import back.tesis.dto.forecast.SimulateResponseDto;
import back.tesis.dto.forecast.VariacionesAplicadasDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class RealMlForecastClient implements MlForecastClient {

    private static final Logger log = LoggerFactory.getLogger(RealMlForecastClient.class);

    private final RestClient mlRestClient;

    public RealMlForecastClient(RestClient mlRestClient) {
        this.mlRestClient = mlRestClient;
    }

    @Override
    public SimulateResponseDto simulate(String empresaId, SimulateRequest request) {
        try {
            return mlRestClient.post()
                    .uri("/simulation/predict")
                    .body(request)
                    .retrieve()
                    .body(SimulateResponseDto.class);
        } catch (RestClientException ex) {
            log.warn("Microservicio ML no disponible ({}), devolviendo resultado simulado para producto {}",
                    ex.getMessage(), request.idProducto());
            return fallback(request);
        }
    }

    private static SimulateResponseDto fallback(SimulateRequest request) {
        return new SimulateResponseDto(
                "simulado_fallback",
                request.idProducto(),
                new VariacionesAplicadasDto(request.precioPct(), request.usdPct(), request.ipcPct()),
                List.of());
    }
}
