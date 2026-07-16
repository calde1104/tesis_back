package back.tesis.security;

import back.tesis.common.exceptions.TenantMismatchException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TenantGuardInterceptorTest {

    private final TenantGuardInterceptor interceptor = new TenantGuardInterceptor();

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void allowsRequestWhenPathEmpresaMatchesToken() {
        TenantContext.set(new TenantContext.AuthenticatedUser("usr-1", "emp-1", "ADMIN"));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .thenReturn(Map.of("empresaId", "emp-1"));

        boolean result = interceptor.preHandle(request, mock(HttpServletResponse.class), new Object());

        assertThat(result).isTrue();
    }

    @Test
    void rejectsRequestWhenPathEmpresaDoesNotMatchToken() {
        TenantContext.set(new TenantContext.AuthenticatedUser("usr-1", "emp-1", "ADMIN"));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .thenReturn(Map.of("empresaId", "emp-OTRA"));

        assertThatThrownBy(() ->
                interceptor.preHandle(request, mock(HttpServletResponse.class), new Object())
        ).isInstanceOf(TenantMismatchException.class);
    }

    @Test
    void allowsRequestWithoutEmpresaPathVariable() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .thenReturn(Map.of());

        boolean result = interceptor.preHandle(request, mock(HttpServletResponse.class), new Object());

        assertThat(result).isTrue();
    }
}
