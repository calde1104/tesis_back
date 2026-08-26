package back.tesis.modules.coldstart;

import back.tesis.common.exceptions.MlServiceUnavailableException;
import back.tesis.common.exceptions.MlValidationException;
import back.tesis.dto.coldstart.ColdStartRequest;
import back.tesis.dto.coldstart.ColdStartResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RealColdStartClient implements ColdStartClient {

    private final RestClient mlRestClient;

    public RealColdStartClient(RestClient mlRestClient) {
        this.mlRestClient = mlRestClient;
    }

    @Override
    public ColdStartResponse predict(ColdStartRequest request) {
        try {
            return mlRestClient.post()
                    .uri("/cold-start/predict")
                    .body(request)
                    .retrieve()
                    .body(ColdStartResponse.class);
        } catch (HttpClientErrorException ex) {
            throw new MlValidationException(
                    "El microservicio de Cold Start (tesis_ia) rechazo el request: " + ex.getResponseBodyAsString());
        } catch (RestClientException ex) {
            throw new MlServiceUnavailableException(
                    "El microservicio de Cold Start (tesis_ia) no respondio: " + ex.getMessage());
        }
    }
}
