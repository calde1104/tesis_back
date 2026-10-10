package back.tesis.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    private final JwtService jwtService = new JwtService(
            "test-secret-key-must-be-at-least-32-bytes-long", 12);
    private final JwtFilter filter = new JwtFilter(jwtService);

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @AfterEach
    void limpiarContextos() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void sinHeaderDeAutorizacionDejaPasarSinAutenticar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void unHeaderQueNoEsBearerSeIgnora() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic algo");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void unTokenValidoAutenticaYPropagaElTenant() throws Exception {
        String token = jwtService.generateToken("usr-1", "emp-1", "ADMIN");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        TenantContext.AuthenticatedUser principal = (TenantContext.AuthenticatedUser) auth.getPrincipal();
        assertThat(principal.usuarioId()).isEqualTo("usr-1");
        assertThat(principal.empresaId()).isEqualTo("emp-1");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void unTokenInvalidoLimpiaElContextoDeSeguridadYSigueLaCadena() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-corrupto");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void siempreLimpiaElTenantContextAunCuandoLaCadenaFalla() throws Exception {
        String token = jwtService.generateToken("usr-1", "emp-1", "ADMIN");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        org.mockito.Mockito.doThrow(new RuntimeException("falla aguas abajo"))
                .when(filterChain).doFilter(request, response);

        try {
            filter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException ignored) {
        }

        assertThat(TenantContext.get()).isNull();
    }
}
