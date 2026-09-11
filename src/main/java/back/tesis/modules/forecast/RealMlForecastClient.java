package back.tesis.modules.forecast;

import back.tesis.common.exceptions.MlServiceUnavailableException;
import back.tesis.common.exceptions.MlValidationException;
import back.tesis.dto.forecast.ModelExplainabilityDto;
import back.tesis.dto.forecast.SimulateRequest;
import back.tesis.dto.forecast.SimulateResponseDto;
import back.tesis.dto.forecast.VariacionesAplicadasDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
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

    @Override
    public ModelExplainabilityDto getModelExplainability() {
        try {
            return mlRestClient.get()
                    .uri("/model/explainability")
                    .retrieve()
                    .body(ModelExplainabilityDto.class);
        } catch (HttpClientErrorException ex) {
            throw new MlValidationException(
                    "El microservicio de ML (tesis_ia) rechazo el request de explicabilidad: "
                            + ex.getResponseBodyAsString());
        } catch (RestClientException ex) {
            throw new MlServiceUnavailableException(
                    "El microservicio de ML (tesis_ia) no respondio al pedido de explicabilidad: " + ex.getMessage());
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
