package back.tesis.modules.context;

import back.tesis.dto.context.MacroDto;
import back.tesis.security.AccessLogService;
import back.tesis.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(ContextController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContextControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContextService contextService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AccessLogService accessLogService;

    @Test
    void getMacroReturnsOkWithList() throws Exception {
        when(contextService.getMacro()).thenReturn(List.of(
                new MacroDto(2024, 1, 5.2f, 3.1f, 7300f, 4.1f, 6.5f, null, null)
        ));

        mockMvc.perform(get("/v1/context/macro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].anio").value(2024));
    }
}
