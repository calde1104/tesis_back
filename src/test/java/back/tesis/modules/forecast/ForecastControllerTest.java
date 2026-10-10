package back.tesis.modules.forecast;

import back.tesis.dto.forecast.ForecastPointDto;
import back.tesis.dto.forecast.ModelExplainabilityDto;
import back.tesis.dto.forecast.SimulateResponseDto;
import back.tesis.dto.forecast.VariacionesAplicadasDto;
import back.tesis.security.AccessLogService;
import back.tesis.security.JwtService;
import back.tesis.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ForecastController.class)
@AutoConfigureMockMvc(addFilters = false)
class ForecastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ForecastService forecastService;

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
    void getForecastDelegaAlServicioSinSkuFiltrado() throws Exception {
        when(forecastService.getForecast("emp-1", null)).thenReturn(
                List.of(new ForecastPointDto(2025, 7, 50.0f, 40.0f, 60.0f)));

        mockMvc.perform(get("/v1/emp-1/forecast"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].anio").value(2025))
                .andExpect(jsonPath("$[0].mes").value(7));
    }

    @Test
    void getForecastPasaElSkuComoParametroOpcional() throws Exception {
        when(forecastService.getForecast("emp-1", "SKU-1")).thenReturn(List.of());

        mockMvc.perform(get("/v1/emp-1/forecast").param("sku", "SKU-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void simulateValidaElCuerpoYDelegaAlServicio() throws Exception {
        when(forecastService.simulate(eq("emp-1"), any())).thenReturn(
                new SimulateResponseDto("success", "prod-1", new VariacionesAplicadasDto(5.0, 0.0, 0.0), List.of()));

        mockMvc.perform(post("/v1/emp-1/forecast/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id_producto\":\"prod-1\",\"precio_pct\":5.0,\"usd_pct\":0.0,\"ipc_pct\":0.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void simulateRechazaUnCuerpoInvalidoConBadRequest() throws Exception {
        mockMvc.perform(post("/v1/emp-1/forecast/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id_producto\":\"\",\"precio_pct\":50.0,\"usd_pct\":0.0,\"ipc_pct\":0.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFeatureImportanceDelegaAlServicio() throws Exception {
        when(forecastService.getFeatureImportance("emp-1")).thenReturn(
                new ModelExplainabilityDto("success", "RandomForestRegressor", 12,
                        Map.of("Historial y Tendencia", 60.0), List.of()));

        mockMvc.perform(get("/v1/emp-1/forecast/feature-importance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo_tipo").value("RandomForestRegressor"));
    }
}
