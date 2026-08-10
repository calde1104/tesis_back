package back.tesis.modules.config;

import back.tesis.dto.config.ConfigResponseDto;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConfigService configService;

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
    void setConfigReturnsOk() throws Exception {
        when(configService.setParametro(eq("emp-1"), any()))
                .thenReturn(new ConfigResponseDto(1, "emp-1", null, "umbral_stock", "100"));

        String body = "{\"parametro\":\"umbral_stock\",\"valor\":\"100\",\"productoId\":null}";

        mockMvc.perform(post("/v1/emp-1/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value("100"));
    }

    @Test
    void getConfigReturnsListOfConfigs() throws Exception {
        when(configService.getConfig("emp-1")).thenReturn(List.of(
                new ConfigResponseDto(1, "emp-1", null, "umbral_stock", "100"),
                new ConfigResponseDto(2, "emp-1", "prod-A", "umbral_stock", "20")));

        mockMvc.perform(get("/v1/emp-1/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].parametro").value("umbral_stock"))
                .andExpect(jsonPath("$[1].productoId").value("prod-A"));
    }
}
