package back.tesis.modules.coldstart;

import back.tesis.dto.coldstart.ColdStartResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ColdStartController.class)
@AutoConfigureMockMvc(addFilters = false)
class ColdStartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ColdStartService coldStartService;

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

    private static final String VALID_BODY = """
            {
              "id_departamento": 2,
              "id_categoria": 8,
              "id_marca": "0032a377-4e1a-4bad-8d0e-dc081ad6afb9",
              "descripcion": "Nombre del nuevo producto",
              "contenido_neto": 1000.0,
              "unidad_medida": "ml",
              "valor_unitario": 28000.0,
              "horizonte_meses": 12
            }
            """;

    @Test
    void predictReturnsOkWithColdStartServiceResponse() throws Exception {
        when(coldStartService.predict(eq("emp-1"), any()))
                .thenReturn(new ColdStartResponse("success", null, null, null));

        mockMvc.perform(post("/v1/emp-1/cold-start/predict")
                        .contentType("application/json")
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void predictRejectsRequestMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/v1/emp-1/cold-start/predict")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void predictRejectsNegativeValorUnitario() throws Exception {
        String bodyConPrecioNegativo = VALID_BODY.replace("28000.0", "-100.0");

        mockMvc.perform(post("/v1/emp-1/cold-start/predict")
                        .contentType("application/json")
                        .content(bodyConPrecioNegativo))
                .andExpect(status().isBadRequest());
    }
}
