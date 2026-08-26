package back.tesis.modules.ingestion;

import back.tesis.common.exceptions.ConflictException;
import back.tesis.common.exceptions.InvalidFileException;
import back.tesis.common.exceptions.ResourceNotFoundException;
import back.tesis.dto.ingestion.IngestionHistoryItemDto;
import back.tesis.entities.ArchivoIngesta;
import back.tesis.entities.AuditoriaIntegridad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private ArchivoIngestaRepository repository;

    @Mock
    private VentaCountRepository ventaMensualCountRepository;

    @Mock
    private VentaSemanalCountRepository ventaSemanalCountRepository;

    @Mock
    private AuditoriaIntegridadRepository auditoriaIntegridadRepository;

    @Mock
    private IngestionFileStorage fileStorage;

    @Mock
    private IngestionProcessor processor;

    private IngestionService newService() {
        return new IngestionService(repository, ventaMensualCountRepository, ventaSemanalCountRepository,
                auditoriaIntegridadRepository, fileStorage, processor);
    }

    @Test
    void uploadRejectsDuplicateHash() {
        MockMultipartFile file = new MockMultipartFile("file", "ventas.csv", "text/csv", "contenido".getBytes());
        when(repository.existsByHashSha256(any())).thenReturn(true);

        IngestionService service = newService();

        assertThatThrownBy(() -> service.upload("emp-1", file, "MENSUAL", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)))
                .isInstanceOf(ConflictException.class);
        verify(processor, never()).procesar(any(), any(), any(), any());
    }

    @Test
    void uploadSavesFileAndTriggersProcessor() {
        MockMultipartFile file = new MockMultipartFile("file", "ventas.csv", "text/csv", "sku,anio,mes,unidades_vendidas,monto_facturado".getBytes());
        when(repository.existsByHashSha256(any())).thenReturn(false);
        when(repository.save(any(ArchivoIngesta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IngestionService service = newService();
        service.upload("emp-1", file, "MENSUAL", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        verify(fileStorage).save(any(), any());
        verify(processor).procesar(any(), eq("emp-1"), eq("MENSUAL"), any());
    }

    @Test
    void uploadRejectsFileWithDisallowedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "ventas.txt", "text/plain", "contenido".getBytes());

        IngestionService service = newService();

        assertThatThrownBy(() -> service.upload("emp-1", file, "MENSUAL", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)))
                .isInstanceOf(InvalidFileException.class);
        verify(processor, never()).procesar(any(), any(), any(), any());
    }

    @Test
    void uploadRejectsDisallowedContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "ventas.csv", "application/pdf", "contenido".getBytes());

        IngestionService service = newService();

        assertThatThrownBy(() -> service.upload("emp-1", file, "MENSUAL", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)))
                .isInstanceOf(InvalidFileException.class);
        verify(processor, never()).procesar(any(), any(), any(), any());
    }

    @Test
    void uploadAcceptsXlsxExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "ventas.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "contenido".getBytes());
        when(repository.existsByHashSha256(any())).thenReturn(false);
        when(repository.save(any(ArchivoIngesta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IngestionService service = newService();
        service.upload("emp-1", file, "MENSUAL", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        verify(processor).procesar(any(), eq("emp-1"), eq("MENSUAL"), any());
    }

    @Test
    void uploadSanitizesOriginalFilename() {
        MockMultipartFile file = new MockMultipartFile("file", "../../etc/ventas <script>.csv", "text/csv", "contenido".getBytes());
        when(repository.existsByHashSha256(any())).thenReturn(false);
        when(repository.save(any(ArchivoIngesta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IngestionService service = newService();
        service.upload("emp-1", file, "MENSUAL", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        ArgumentCaptor<ArchivoIngesta> captor = ArgumentCaptor.forClass(ArchivoIngesta.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNombreArchivo()).isEqualTo("ventas__script_.csv");
    }

    @Test
    void borrarSetsEstadoEliminadoWithoutDeletingRow() {
        ArchivoIngesta archivo = new ArchivoIngesta("emp-1", "ventas.csv", "hash123", "MENSUAL",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        when(repository.findByIdAndEmpresaId("arch-1", "emp-1")).thenReturn(Optional.of(archivo));
        when(repository.save(archivo)).thenReturn(archivo);

        IngestionService service = newService();
        service.borrar("emp-1", "arch-1");

        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_ELIMINADO);
    }

    @Test
    void borrarThrowsWhenArchivoNotFoundForEmpresa() {
        when(repository.findByIdAndEmpresaId("arch-x", "emp-1")).thenReturn(Optional.empty());

        IngestionService service = newService();

        assertThatThrownBy(() -> service.borrar("emp-1", "arch-x"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void resyncReadsStoredFileAndTriggersProcessorAgain() {
        ArchivoIngesta archivo = new ArchivoIngesta("emp-1", "ventas.csv", "hash1", "MENSUAL",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        when(repository.findByIdAndEmpresaId("arch-1", "emp-1")).thenReturn(Optional.of(archivo));
        when(repository.save(archivo)).thenReturn(archivo);
        byte[] contenido = "sku,anio,mes,unidades_vendidas,monto_facturado".getBytes();
        when(fileStorage.read(archivo.getId())).thenReturn(contenido);

        IngestionService service = newService();
        service.resync("emp-1", "arch-1");

        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_PENDIENTE);
        verify(processor).procesar(archivo.getId(), "emp-1", "MENSUAL", contenido);
    }

    @Test
    void getHistoryIncludesCantRegistrosFromVentasMensualesYSemanales() {
        ArchivoIngesta archivo = new ArchivoIngesta("emp-1", "ventas.csv", "hash1", "MENSUAL",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        Page<ArchivoIngesta> page = new PageImpl<>(List.of(archivo));
        when(repository.findByEmpresaIdOrderByFechaRecepcionDesc(eq("emp-1"), any())).thenReturn(page);
        when(ventaMensualCountRepository.countByArchivoId(archivo.getId())).thenReturn(150L);
        when(ventaSemanalCountRepository.countByArchivoId(archivo.getId())).thenReturn(0L);

        IngestionService service = newService();

        Page<IngestionHistoryItemDto> result = service.getHistory("emp-1", PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).cantRegistros()).isEqualTo(150L);
    }

    @Test
    void uploadCreaTambienUnSelloDeAuditoriaIntegridadValido() {
        MockMultipartFile file = new MockMultipartFile("file", "ventas.csv", "text/csv", "contenido".getBytes());
        when(repository.existsByHashSha256(any())).thenReturn(false);
        when(repository.save(any(ArchivoIngesta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IngestionService service = newService();

        service.upload("emp-1", file, "MENSUAL", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        ArgumentCaptor<AuditoriaIntegridad> captor = ArgumentCaptor.forClass(AuditoriaIntegridad.class);
        verify(auditoriaIntegridadRepository).save(captor.capture());
        assertThat(captor.getValue().getResultadoValidacion()).isEqualTo(AuditoriaIntegridad.RESULTADO_VALIDO);
        assertThat(captor.getValue().getHashSha256()).isNotBlank();
    }
}
