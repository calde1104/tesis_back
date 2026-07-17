package back.tesis.modules.config;

import back.tesis.dto.config.SetConfigRequest;
import back.tesis.entities.ConfigSistema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {

    @Mock
    private ConfigSistemaRepository repository;

    @Test
    void createsNewConfigWhenNoneExists() {
        when(repository.findByEmpresaIdAndParametro("emp-1", "umbral_stock")).thenReturn(Optional.empty());
        when(repository.save(org.mockito.ArgumentMatchers.any(ConfigSistema.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConfigService service = new ConfigService(repository);

        var result = service.setParametro("emp-1", new SetConfigRequest("umbral_stock", "100", null));

        assertThat(result.parametro()).isEqualTo("umbral_stock");
        assertThat(result.valor()).isEqualTo("100");
    }

    @Test
    void updatesExistingConfigValue() {
        ConfigSistema existente = new ConfigSistema("emp-1", null, "umbral_stock", "50");
        when(repository.findByEmpresaIdAndParametro("emp-1", "umbral_stock")).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        ConfigService service = new ConfigService(repository);

        var result = service.setParametro("emp-1", new SetConfigRequest("umbral_stock", "200", null));

        assertThat(result.valor()).isEqualTo("200");
        verify(repository).save(existente);
    }
}
