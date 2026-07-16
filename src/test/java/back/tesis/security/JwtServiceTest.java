package back.tesis.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "test-secret-key-must-be-at-least-32-bytes-long", 12);

    @Test
    void generatesAndParsesTokenRoundTrip() {
        String token = jwtService.generateToken("usr-1", "emp-1", "ADMIN");

        TenantContext.AuthenticatedUser parsed = jwtService.parseToken(token);

        assertThat(parsed.usuarioId()).isEqualTo("usr-1");
        assertThat(parsed.empresaId()).isEqualTo("emp-1");
        assertThat(parsed.rol()).isEqualTo("ADMIN");
    }
}
