package back.tesis.modules.marketshare;

import back.tesis.dto.marketshare.CategoriaMarketShareSummaryDto;
import back.tesis.dto.marketshare.EmpresaShareDto;
import back.tesis.dto.marketshare.MarketShareOverviewPointDto;
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

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MarketShareController.class)
@AutoConfigureMockMvc(addFilters = false)
class MarketShareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MarketShareService marketShareService;

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
    void getOverviewReturnsOkWithSeries() throws Exception {
        when(marketShareService.getOverview("emp-1", null, null, null, null, null)).thenReturn(
                List.of(new MarketShareOverviewPointDto(2024, 1, 0.35)));

        mockMvc.perform(get("/v1/emp-1/market-share/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].marketShareUnidadesPromedio").value(0.35));
    }

    @Test
    void getOverviewPassesQueryParamsAsFilters() throws Exception {
        when(marketShareService.getOverview("emp-1", "marca-1", 10, 5, List.of(2023, 2024), List.of(1, 2)))
                .thenReturn(List.of());

        mockMvc.perform(get("/v1/emp-1/market-share/overview")
                        .param("marcaId", "marca-1")
                        .param("categoriaId", "10")
                        .param("departamentoId", "5")
                        .param("anios", "2023", "2024")
                        .param("meses", "1", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getCategoriesSummaryReturnsOkWithBreakdown() throws Exception {
        when(marketShareService.getCategoriesSummary("emp-1", 2024, 3, null)).thenReturn(List.of(
                new CategoriaMarketShareSummaryDto("Whiskyes", 15.4, List.of(
                        new EmpresaShareDto("Pernod Ricard", 40.5),
                        new EmpresaShareDto("Nuestra Empresa", 15.4)))));

        mockMvc.perform(get("/v1/emp-1/market-share/categories-summary")
                        .param("anio", "2024")
                        .param("mes", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Whiskyes"))
                .andExpect(jsonPath("$[0].nuestraCuota").value(15.4))
                .andExpect(jsonPath("$[0].rankingCompetencia[0].empresaNombre").value("Pernod Ricard"));
    }
}
