package back.tesis.modules.notificaciones;

import back.tesis.dto.notificaciones.NotificacionDto;
import back.tesis.entities.EventoSistema;
import back.tesis.security.AccessLogService;
import back.tesis.security.JwtService;
import back.tesis.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificacionesController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificacionesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificacionesService notificacionesService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AccessLogService accessLogService;

    @BeforeEach
    void setUp() {
        TenantContext.set(new TenantContext.AuthenticatedUser("user-1", "emp-1", "ADMIN"));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void listarReturnsPaginatedNotificaciones() throws Exception {
        NotificacionDto dto = new NotificacionDto(
                "evt-1", "emp-1", "prod-1", "UMBRAL_DEMANDA_SUPERADO", EventoSistema.SEVERIDAD_WARNING,
                "{\"umbralDemandaMaxima\":400.0}", EventoSistema.ESTADO_ACTIVA, LocalDateTime.of(2026, 8, 26, 10, 0));
        when(notificacionesService.listar(eq("emp-1"), any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/v1/emp-1/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].tipoEvento").value("UMBRAL_DEMANDA_SUPERADO"))
                .andExpect(jsonPath("$.content[0].payload.umbralDemandaMaxima").value(400.0));
    }
}
