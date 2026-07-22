package back.tesis.modules.sales;

import back.tesis.dto.sales.SalesSummaryDto;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SalesController.class)
@AutoConfigureMockMvc(addFilters = false)
class SalesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SalesService salesService;

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
    void getSummaryReturnsOk() throws Exception {
        when(salesService.getSummary(eq("emp-1"), any(), any()))
                .thenReturn(new SalesSummaryDto(100L, new BigDecimal("999.00")));

        mockMvc.perform(get("/v1/emp-1/sales/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUnidades").value(100));
    }
}
