package back.tesis.common;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void dataIntegrityViolationMapsTo409WithClearBody() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/v1/emp-1/config");

        ResponseEntity<ApiError> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("could not execute statement; FK constraint"),
                request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(409);
        assertThat(body.error()).isEqualTo("Conflict");
        assertThat(body.message()).contains("integridad de datos");
        assertThat(body.path()).isEqualTo("/v1/emp-1/config");
        assertThat(body.message()).doesNotContain("FK constraint");
    }
}
