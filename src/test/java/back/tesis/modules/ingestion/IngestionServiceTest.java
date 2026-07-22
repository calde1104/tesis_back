package back.tesis.modules.ingestion;

import back.tesis.common.exceptions.ConflictException;
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

    @Test
    void uploadRejectsDuplicateHash() {
        MockMultipartFile file = new MockMultipartFile("file", "ventas.xlsx", "application/octet-stream", "contenido".getBytes());
        when(repository.existsByHashSha256(any())).thenReturn(true);

        IngestionService service = new IngestionService(repository, ventaMensualCountRepository, ventaSemanalCountRepository, auditoriaIntegridadRepository);

        assertThatThrownBy(() -> service.upload("emp-1", file, "MENSUAL", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void borrarSetsEstadoEliminadoWithoutDeletingRow() {
        ArchivoIngesta archivo = new ArchivoIngesta("emp-1", "ventas.xlsx", "hash123", "MENSUAL",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        when(repository.findByIdAndEmpresaId("arch-1", "emp-1")).thenReturn(Optional.of(archivo));
        when(repository.save(archivo)).thenReturn(archivo);

        IngestionService service = new IngestionService(repository, ventaMensualCountRepository, ventaSemanalCountRepository, auditoriaIntegridadRepository);
        service.borrar("emp-1", "arch-1");

        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_ELIMINADO);
    }

    @Test
    void borrarThrowsWhenArchivoNotFoundForEmpresa() {
        when(repository.findByIdAndEmpresaId("arch-x", "emp-1")).thenReturn(Optional.empty());

        IngestionService service = new IngestionService(repository, ventaMensualCountRepository, ventaSemanalCountRepository, auditoriaIntegridadRepository);

        assertThatThrownBy(() -> service.borrar("emp-1", "arch-x"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getHistoryIncludesCantRegistrosFromVentasMensualesYSemanales() {
        ArchivoIngesta archivo = new ArchivoIngesta("emp-1", "ventas.xlsx", "hash1", "MENSUAL",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        Page<ArchivoIngesta> page = new PageImpl<>(List.of(archivo));
        when(repository.findByEmpresaIdOrderByFechaRecepcionDesc(eq("emp-1"), any())).thenReturn(page);
        when(ventaMensualCountRepository.countByArchivoId(archivo.getId())).thenReturn(150L);
        when(ventaSemanalCountRepository.countByArchivoId(archivo.getId())).thenReturn(0L);

        IngestionService service = new IngestionService(repository, ventaMensualCountRepository, ventaSemanalCountRepository, auditoriaIntegridadRepository);

        Page<IngestionHistoryItemDto> result = service.getHistory("emp-1", PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).cantRegistros()).isEqualTo(150L);
    }

    @Test
    void uploadCreaTambienUnSelloDeAuditoriaIntegridadValido() {
        MockMultipartFile file = new MockMultipartFile("file", "ventas.xlsx", "application/octet-stream", "contenido".getBytes());
        when(repository.existsByHashSha256(any())).thenReturn(false);
        when(repository.save(any(ArchivoIngesta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IngestionService service = new IngestionService(
                repository, ventaMensualCountRepository, ventaSemanalCountRepository, auditoriaIntegridadRepository);

        service.upload("emp-1", file, "MENSUAL", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        ArgumentCaptor<AuditoriaIntegridad> captor = ArgumentCaptor.forClass(AuditoriaIntegridad.class);
        verify(auditoriaIntegridadRepository).save(captor.capture());
        assertThat(captor.getValue().getResultadoValidacion()).isEqualTo(AuditoriaIntegridad.RESULTADO_VALIDO);
        assertThat(captor.getValue().getHashSha256()).isNotBlank();
    }
}
