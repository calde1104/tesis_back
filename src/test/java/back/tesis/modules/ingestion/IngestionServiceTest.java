package back.tesis.modules.ingestion;

import back.tesis.common.exceptions.ConflictException;
import back.tesis.common.exceptions.ResourceNotFoundException;
import back.tesis.entities.ArchivoIngesta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private ArchivoIngestaRepository repository;

    @Test
    void uploadRejectsDuplicateHash() {
        MockMultipartFile file = new MockMultipartFile("file", "ventas.xlsx", "application/octet-stream", "contenido".getBytes());
        when(repository.existsByHashSha256(any())).thenReturn(true);

        IngestionService service = new IngestionService(repository);

        assertThatThrownBy(() -> service.upload("emp-1", file, "MENSUAL", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void borrarSetsEstadoEliminadoWithoutDeletingRow() {
        ArchivoIngesta archivo = new ArchivoIngesta("emp-1", "ventas.xlsx", "hash123", "MENSUAL",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        when(repository.findByIdAndEmpresaId("arch-1", "emp-1")).thenReturn(Optional.of(archivo));
        when(repository.save(archivo)).thenReturn(archivo);

        IngestionService service = new IngestionService(repository);
        service.borrar("emp-1", "arch-1");

        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_ELIMINADO);
    }

    @Test
    void borrarThrowsWhenArchivoNotFoundForEmpresa() {
        when(repository.findByIdAndEmpresaId("arch-x", "emp-1")).thenReturn(Optional.empty());

        IngestionService service = new IngestionService(repository);

        assertThatThrownBy(() -> service.borrar("emp-1", "arch-x"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
