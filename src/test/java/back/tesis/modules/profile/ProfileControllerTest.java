package back.tesis.modules.profile;

import back.tesis.dto.auth.EmpresaResumenDto;
import back.tesis.dto.profile.ProfileResponse;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ProfileService profileService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private AccessLogService accessLogService;

    private final EmpresaResumenDto empresa = new EmpresaResumenDto("emp-1", "ACME", "30-1");

    @BeforeEach
    void setUp() {
        TenantContext.set(new TenantContext.AuthenticatedUser("user-1", "emp-1", "ADMIN"));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getProfileDevuelve200() throws Exception {
        when(profileService.getProfile("user-1"))
                .thenReturn(new ProfileResponse("user-1", "user@tesis.local", "ADMIN", empresa, null));

        mockMvc.perform(get("/v1/emp-1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@tesis.local"))
                .andExpect(jsonPath("$.foto").doesNotExist());
    }

    @Test
    void putFotoDevuelve200ConPerfilActualizado() throws Exception {
        when(profileService.updateFoto(eq("user-1"), any()))
                .thenReturn(new ProfileResponse(
                        "user-1", "user@tesis.local", "ADMIN", empresa, "data:image/png;base64,AQID"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/v1/emp-1/profile/foto")
                        .file(file)
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foto").value("data:image/png;base64,AQID"));
    }

    @Test
    void deleteFotoDevuelve204() throws Exception {
        mockMvc.perform(delete("/v1/emp-1/profile/foto"))
                .andExpect(status().isNoContent());

        verify(profileService).deleteFoto("user-1");
    }
}
