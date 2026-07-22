package back.tesis.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessLogInterceptorTest {

    @Mock
    private AccessLogService accessLogService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private AccessLogInterceptor interceptor;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        interceptor = new AccessLogInterceptor(accessLogService);
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void noLogueaNadaSiNoHayTenantContext() {
        interceptor.afterCompletion(request, response, new Object(), null);

        verifyNoInteractions(accessLogService);
    }

    @Test
    void logueaOkParaStatus2xx() {
        TenantContext.set(new TenantContext.AuthenticatedUser("usr-1", "emp-1", "ADMIN"));
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/v1/emp-1/sales");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(accessLogService).registrar("usr-1", "emp-1", "GET", "/v1/emp-1/sales", "127.0.0.1", "OK");
    }

    @Test
    void logueaDenegadoParaStatus403() {
        TenantContext.set(new TenantContext.AuthenticatedUser("usr-1", "emp-1", "ADMIN"));
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/v1/emp-2/sales");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(403);

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(accessLogService).registrar(eq("usr-1"), eq("emp-1"), eq("GET"), eq("/v1/emp-2/sales"), eq("127.0.0.1"), eq("DENEGADO"));
    }

    @Test
    void logueaErrorParaOtrosStatus() {
        TenantContext.set(new TenantContext.AuthenticatedUser("usr-1", "emp-1", "ADMIN"));
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/v1/emp-1/ingestion/upload");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(409);

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(accessLogService).registrar(eq("usr-1"), eq("emp-1"), eq("POST"), eq("/v1/emp-1/ingestion/upload"), eq("127.0.0.1"), eq("ERROR"));
    }
}
