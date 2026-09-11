package back.tesis.modules.forecast;

import back.tesis.common.exceptions.MlServiceUnavailableException;
import back.tesis.common.exceptions.MlValidationException;
import back.tesis.dto.forecast.ModelExplainabilityDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealMlForecastClientTest {

    @Mock
    private RestClient mlRestClient;

    @Mock
    private RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RequestHeadersSpec requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;

    @Test
    @SuppressWarnings("unchecked")
    void getModelExplainabilityReturnsResponseFromMlService() {
        ModelExplainabilityDto response = new ModelExplainabilityDto(
                "success", "LinearRegression", 11, Map.of("Historial y Tendencia", 55.2), List.of());

        when(mlRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/model/explainability")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ModelExplainabilityDto.class)).thenReturn(response);

        RealMlForecastClient client = new RealMlForecastClient(mlRestClient);

        ModelExplainabilityDto result = client.getModelExplainability();

        assertThat(result.status()).isEqualTo("success");
        assertThat(result.modeloTipo()).isEqualTo("LinearRegression");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getModelExplainabilityThrowsMlServiceUnavailableWhenMlServiceIsDown() {
        when(mlRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/model/explainability")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(new ResourceAccessException("timeout"));

        RealMlForecastClient client = new RealMlForecastClient(mlRestClient);

        assertThatThrownBy(client::getModelExplainability)
                .isInstanceOf(MlServiceUnavailableException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getModelExplainabilityThrowsMlValidationWhenMlServiceRejectsRequest() {
        when(mlRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/model/explainability")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(
                HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request",
                        null, "{\"detail\":\"Error al obtener explicabilidad del modelo\"}".getBytes(), null));

        RealMlForecastClient client = new RealMlForecastClient(mlRestClient);

        assertThatThrownBy(client::getModelExplainability)
                .isInstanceOf(MlValidationException.class);
    }
}
