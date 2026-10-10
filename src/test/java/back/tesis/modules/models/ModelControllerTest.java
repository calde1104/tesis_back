package back.tesis.modules.models;

import back.tesis.dto.models.ModelVersionDto;
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

@WebMvcTest(ModelController.class)
@AutoConfigureMockMvc(addFilters = false)
class ModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModelService modelService;

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
    void listModelsDelegaAlServicioYDevuelveLaLista() throws Exception {
        when(modelService.listModels("emp-1")).thenReturn(
                List.of(new ModelVersionDto("mv-1", "champion_rf_2025_12", 209.22f, 2509.1f, 15.66f, true)));

        mockMvc.perform(get("/v1/emp-1/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("mv-1"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }
}
