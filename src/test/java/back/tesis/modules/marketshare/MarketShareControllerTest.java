package back.tesis.modules.marketshare;

import back.tesis.dto.marketshare.MarketShareOverviewPointDto;
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
        when(marketShareService.getOverview("emp-1")).thenReturn(
                List.of(new MarketShareOverviewPointDto(2024, 1, 0.35)));

        mockMvc.perform(get("/v1/emp-1/market-share/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].marketShareUnidadesPromedio").value(0.35));
    }
}
