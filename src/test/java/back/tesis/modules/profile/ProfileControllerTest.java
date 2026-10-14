package back.tesis.modules.profile;

import back.tesis.common.exceptions.InvalidFileException;
import back.tesis.common.exceptions.ResourceNotFoundException;
import back.tesis.dto.auth.EmpresaResumenDto;
import back.tesis.dto.profile.ProfileResponse;
import back.tesis.security.AccessLogService;
import back.tesis.security.JwtService;
import back.tesis.security.TenantContext;
import back.tesis.security.TenantGuardInterceptor;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
@Import({back.tesis.common.GlobalExceptionHandler.class, ProfileControllerTest.TenantGuardTestConfig.class})
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
                .andExpect(jsonPath("$.foto").value(Matchers.nullValue()));
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
    void putFotoConArchivoInvalidoDevuelve400() throws Exception {
        when(profileService.updateFoto(eq("user-1"), any()))
                .thenThrow(new InvalidFileException("Tipo de imagen no permitido"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[]{1});

        mockMvc.perform(multipart("/v1/emp-1/profile/foto")
                        .file(file)
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProfileUsuarioInexistenteDevuelve404() throws Exception {
        when(profileService.getProfile("user-1"))
                .thenThrow(new ResourceNotFoundException("Usuario no encontrado"));

        mockMvc.perform(get("/v1/emp-1/profile"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProfileConEmpresaDistintaDevuelve403() throws Exception {
        mockMvc.perform(get("/v1/emp-2/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteFotoDevuelve204() throws Exception {
        mockMvc.perform(delete("/v1/emp-1/profile/foto"))
                .andExpect(status().isNoContent());

        verify(profileService).deleteFoto("user-1");
    }

    @Test
    void deleteCuentaDevuelve204() throws Exception {
        mockMvc.perform(delete("/v1/emp-1/profile"))
                .andExpect(status().isNoContent());

        verify(profileService).eliminarCuenta("user-1");
    }

    @TestConfiguration
    static class TenantGuardTestConfig implements WebMvcConfigurer {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new TenantGuardInterceptor());
        }
    }
}
