package back.tesis.modules.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelServiceTest {

    @Mock
    private ModeloVersionRepository repository;

    @Test
    void listModelsReturnsEmptyListWhenNoneTrainedYet() {
        when(repository.findByEmpresaIdOrderByFechaEntrenamientoDesc("emp-1")).thenReturn(List.of());

        ModelService service = new ModelService(repository);

        assertThat(service.listModels("emp-1")).isEmpty();
    }
}
