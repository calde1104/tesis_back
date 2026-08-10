package back.tesis.modules.config;

import back.tesis.dto.config.SetConfigRequest;
import back.tesis.entities.ConfigSistema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
        when(repository.findByEmpresaIdAndProductoIdAndParametro("emp-1", null, "umbral_stock"))
                .thenReturn(Optional.empty());
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
        when(repository.findByEmpresaIdAndProductoIdAndParametro("emp-1", null, "umbral_stock"))
                .thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        ConfigService service = new ConfigService(repository);

        var result = service.setParametro("emp-1", new SetConfigRequest("umbral_stock", "200", null));

        assertThat(result.valor()).isEqualTo("200");
        verify(repository).save(existente);
    }

    @Test
    void createsSeparateConfigPerProductoInsteadOfCollidingOnSameParametro() {
        when(repository.findByEmpresaIdAndProductoIdAndParametro("emp-1", "prod-A", "umbral_stock"))
                .thenReturn(Optional.empty());
        when(repository.save(org.mockito.ArgumentMatchers.any(ConfigSistema.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConfigService service = new ConfigService(repository);

        var result = service.setParametro("emp-1", new SetConfigRequest("umbral_stock", "100", "prod-A"));

        assertThat(result.productoId()).isEqualTo("prod-A");
        assertThat(result.valor()).isEqualTo("100");
        verify(repository).findByEmpresaIdAndProductoIdAndParametro("emp-1", "prod-A", "umbral_stock");
    }

    @Test
    void updatesExistingConfigForThatSpecificProductoOnly() {
        ConfigSistema existente = new ConfigSistema("emp-1", "prod-A", "umbral_stock", "50");
        when(repository.findByEmpresaIdAndProductoIdAndParametro("emp-1", "prod-A", "umbral_stock"))
                .thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        ConfigService service = new ConfigService(repository);

        var result = service.setParametro("emp-1", new SetConfigRequest("umbral_stock", "200", "prod-A"));

        assertThat(result.valor()).isEqualTo("200");
        assertThat(result.productoId()).isEqualTo("prod-A");
        verify(repository, org.mockito.Mockito.never())
                .findByEmpresaIdAndProductoIdAndParametro("emp-1", null, "umbral_stock");
    }

    @Test
    void getConfigReturnsAllConfigsOfEmpresaAsDto() {
        when(repository.findByEmpresaIdOrderByParametroAsc("emp-1")).thenReturn(List.of(
                new ConfigSistema("emp-1", null, "umbral_stock", "100"),
                new ConfigSistema("emp-1", "prod-A", "umbral_stock", "20")));

        ConfigService service = new ConfigService(repository);

        var result = service.getConfig("emp-1");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(dto -> dto.productoId()).containsExactly(null, "prod-A");
        assertThat(result).allSatisfy(dto -> {
            assertThat(dto.empresaId()).isEqualTo("emp-1");
            assertThat(dto.parametro()).isEqualTo("umbral_stock");
        });
    }

    @Test
    void getConfigReturnsEmptyListWhenEmpresaHasNoConfig() {
        when(repository.findByEmpresaIdOrderByParametroAsc("emp-1")).thenReturn(List.of());

        ConfigService service = new ConfigService(repository);

        assertThat(service.getConfig("emp-1")).isEmpty();
    }
}
