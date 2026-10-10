package back.tesis.modules.ingestion;

import back.tesis.dto.ingestion.IngestionHistoryItemDto;
import back.tesis.dto.ingestion.UploadResponseDto;
import back.tesis.security.AccessLogService;
import back.tesis.security.JwtService;
import back.tesis.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestionController.class)
@AutoConfigureMockMvc(addFilters = false)
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngestionService ingestionService;

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
    void uploadDelegaAlServicioYDevuelveLaRespuesta() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "ventas.csv", "text/csv", "sku,anio,mes,unidades_vendidas\n".getBytes());
        when(ingestionService.upload(eq("emp-1"), any(), eq("MENSUAL"),
                eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31))))
                .thenReturn(new UploadResponseDto("arch-1", "PENDIENTE", "Archivo recibido, procesando"));

        mockMvc.perform(multipart("/v1/emp-1/ingestion/upload")
                        .file(file)
                        .param("granularidad", "MENSUAL")
                        .param("periodoDesde", "2024-01-01")
                        .param("periodoHasta", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archivoId").value("arch-1"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void getHistoryDevuelveLaPaginaMapeada() throws Exception {
        IngestionHistoryItemDto item = new IngestionHistoryItemDto(
                "arch-1", "ventas.csv", "OK", "MENSUAL",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), LocalDateTime.now(), 10);
        when(ingestionService.getHistory(eq("emp-1"), any()))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/v1/emp-1/ingestion/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].archivoId").value("arch-1"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void resyncDelegaAlServicioYDevuelveLaRespuesta() throws Exception {
        when(ingestionService.resync("emp-1", "arch-1"))
                .thenReturn(new UploadResponseDto("arch-1", "PENDIENTE", "Reintentando procesamiento"));

        mockMvc.perform(post("/v1/emp-1/ingestion/arch-1/resync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Reintentando procesamiento"));
    }

    @Test
    void borrarDelegaAlServicioYDevuelveNoContent() throws Exception {
        mockMvc.perform(delete("/v1/emp-1/ingestion/arch-1/borrado"))
                .andExpect(status().isNoContent());

        verify(ingestionService).borrar("emp-1", "arch-1");
    }
}
