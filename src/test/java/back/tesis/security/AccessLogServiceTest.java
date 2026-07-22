package back.tesis.security;

import back.tesis.entities.LogAcceso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccessLogServiceTest {

    @Mock
    private LogAccesoRepository repository;

    @Test
    void registrarGuardaUnLogAccesoConLosDatosRecibidos() {
        AccessLogService service = new AccessLogService(repository);

        service.registrar("usr-1", "emp-1", "GET", "/v1/emp-1/sales", "127.0.0.1", LogAcceso.RESULTADO_OK);

        ArgumentCaptor<LogAcceso> captor = ArgumentCaptor.forClass(LogAcceso.class);
        verify(repository).save(captor.capture());
        LogAcceso guardado = captor.getValue();
        assertThat(guardado.getUsuarioId()).isEqualTo("usr-1");
        assertThat(guardado.getEmpresaId()).isEqualTo("emp-1");
        assertThat(guardado.getAccion()).isEqualTo("GET");
        assertThat(guardado.getEndpoint()).isEqualTo("/v1/emp-1/sales");
        assertThat(guardado.getIpOrigen()).isEqualTo("127.0.0.1");
        assertThat(guardado.getResultado()).isEqualTo("OK");
    }
}
