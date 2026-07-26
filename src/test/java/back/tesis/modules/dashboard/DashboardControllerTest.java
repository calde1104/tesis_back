package back.tesis.modules.dashboard;

import back.tesis.dto.dashboard.KpisDto;
import back.tesis.dto.dashboard.MarketShareCompositionDto;
import back.tesis.security.AccessLogService;
import back.tesis.security.JwtService;
import back.tesis.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

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
    void getKpisReturnsOk() throws Exception {
        when(dashboardService.getKpis("emp-1", 2024, 3))
                .thenReturn(new KpisDto(2024, 3, 500L, new BigDecimal("1000.00"), null, 0.4, 5.0, 25.0));

        mockMvc.perform(get("/v1/emp-1/dashboard/kpis").param("anio", "2024").param("mes", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventasUnidades").value(500));
    }

    @Test
    void getMarketShareCompositionPassesQueryParamsAsFilters() throws Exception {
        when(dashboardService.getMarketShareComposition("emp-1", 2024, 3, "marca-1", 10, 5))
                .thenReturn(List.of());

        mockMvc.perform(get("/v1/emp-1/dashboard/market-share-composition")
                        .param("anio", "2024")
                        .param("mes", "3")
                        .param("marcaId", "marca-1")
                        .param("categoriaId", "10")
                        .param("departamentoId", "5"))
                .andExpect(status().isOk());
    }
}
