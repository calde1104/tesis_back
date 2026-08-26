package back.tesis.modules.coldstart;

import back.tesis.common.exceptions.MlServiceUnavailableException;
import back.tesis.common.exceptions.MlValidationException;
import back.tesis.dto.coldstart.ColdStartRequest;
import back.tesis.dto.coldstart.ColdStartResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.RequestBodyUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealColdStartClientTest {

    @Mock
    private RestClient mlRestClient;

    @Mock
    private RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RequestBodySpec requestBodySpec;

    @Mock
    private ResponseSpec responseSpec;

    @Test
    void predictReturnsResponseFromMlService() {
        ColdStartRequest request = new ColdStartRequest(2, 8, "marca-1", "Producto nuevo", 1000.0, "ml", 28000.0, 12);
        ColdStartResponse response = new ColdStartResponse("success", null, null, null);

        when(mlRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/cold-start/predict")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ColdStartResponse.class)).thenReturn(response);

        RealColdStartClient client = new RealColdStartClient(mlRestClient);

        ColdStartResponse result = client.predict(request);

        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void predictThrowsMlServiceUnavailableWhenMlServiceIsDown() {
        ColdStartRequest request = new ColdStartRequest(2, 8, "marca-1", "Producto nuevo", 1000.0, "ml", 28000.0, 12);

        when(mlRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/cold-start/predict")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new ResourceAccessException("timeout"));

        RealColdStartClient client = new RealColdStartClient(mlRestClient);

        assertThatThrownBy(() -> client.predict(request))
                .isInstanceOf(MlServiceUnavailableException.class);
    }

    @Test
    void predictThrowsMlValidationWhenMlServiceRejectsInvalidInput() {
        ColdStartRequest request = new ColdStartRequest(2, 8, "marca-1", "Producto nuevo", 1000.0, "ml", 28000.0, 12);

        when(mlRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/cold-start/predict")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(
                HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity",
                        null, "{\"detail\":\"valor_unitario debe ser mayor a 0\"}".getBytes(), null));

        RealColdStartClient client = new RealColdStartClient(mlRestClient);

        assertThatThrownBy(() -> client.predict(request))
                .isInstanceOf(MlValidationException.class);
    }
}
