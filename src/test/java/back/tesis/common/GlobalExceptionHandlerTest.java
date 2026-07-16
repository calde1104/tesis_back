package back.tesis.common;

import back.tesis.common.exceptions.ConflictException;
import back.tesis.common.exceptions.InvalidCredentialsException;
import back.tesis.common.exceptions.InvalidFileException;
import back.tesis.common.exceptions.ResourceNotFoundException;
import back.tesis.common.exceptions.TenantMismatchException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsResourceNotFoundTo404() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/x/sales");

        ResponseEntity<ApiError> response = handler.handleNotFound(
                new ResourceNotFoundException("no existe"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("no existe");
    }

    @Test
    void mapsTenantMismatchTo403() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/x/sales");

        ResponseEntity<ApiError> response = handler.handleTenantMismatch(
                new TenantMismatchException("no coincide"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void mapsInvalidCredentialsTo401() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/auth/login");

        ResponseEntity<ApiError> response = handler.handleInvalidCredentials(
                new InvalidCredentialsException("credenciales invalidas"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void mapsConflictTo409() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/x/ingestion/upload");

        ResponseEntity<ApiError> response = handler.handleConflict(
                new ConflictException("hash duplicado"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void mapsInvalidFileTo400() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/x/ingestion/upload");

        ResponseEntity<ApiError> response = handler.handleInvalidFile(
                new InvalidFileException("no se pudo calcular el hash"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
