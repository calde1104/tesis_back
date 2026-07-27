package back.tesis.modules.system;

import back.tesis.dto.system.SystemInfoDto;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemInfoController.class)
@AutoConfigureMockMvc(addFilters = false)
class SystemInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SystemInfoService systemInfoService;

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
    void getSystemInfoReturnsOk() throws Exception {
        when(systemInfoService.getSystemInfo("emp-1")).thenReturn(new SystemInfoDto(
                "Acme SA", "80012345-6", "emp-1", "ADMIN", null,
                null, "v3", 12.5f, 3.1f, 4.2f, "2023-01-01 - 2024-12-01", null, null,
                "MySQL 8.0.36", "UP", 5));

        mockMvc.perform(get("/v1/emp-1/system-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razonSocial").value("Acme SA"))
                .andExpect(jsonPath("$.dbStatus").value("UP"));
    }
}
