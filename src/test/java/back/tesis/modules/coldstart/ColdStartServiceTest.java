package back.tesis.modules.coldstart;

import back.tesis.dto.coldstart.ColdStartRequest;
import back.tesis.dto.coldstart.ColdStartResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColdStartServiceTest {

    @Mock
    private ColdStartClient coldStartClient;

    @Test
    void predictDelegatesToColdStartClient() {
        ColdStartRequest request = new ColdStartRequest(2, 8, "marca-1", "Producto nuevo", 1000.0, "ml", 28000.0, 12);
        when(coldStartClient.predict(request))
                .thenReturn(new ColdStartResponse("success", null, null, null));

        ColdStartService service = new ColdStartService(coldStartClient);

        ColdStartResponse result = service.predict("emp-1", request);

        assertThat(result.status()).isEqualTo("success");
    }
}
