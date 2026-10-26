package back.tesis.modules.config;

import back.tesis.dto.config.SetConfigRequest;
import back.tesis.entities.ConfigSistema;
import back.tesis.modules.notificaciones.NotificacionesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {

    @Mock
    private ConfigSistemaRepository repository;
    @Mock
    private NotificacionesService notificacionesService;

    private ConfigService newService() {
        return new ConfigService(repository, notificacionesService);
    }

    @Test
    void createsNewConfigWhenNoneExists() {
        when(repository.findByEmpresaIdAndProductoIdAndParametro("emp-1", null, "umbral_stock"))
                .thenReturn(Optional.empty());
        when(repository.save(org.mockito.ArgumentMatchers.any(ConfigSistema.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = newService().setParametro("emp-1", new SetConfigRequest("umbral_stock", "100", null));

        assertThat(result.parametro()).isEqualTo("umbral_stock");
        assertThat(result.valor()).isEqualTo("100");
    }

    @Test
    void updatesExistingConfigValue() {
        ConfigSistema existente = new ConfigSistema("emp-1", null, "umbral_stock", "50");
        when(repository.findByEmpresaIdAndProductoIdAndParametro("emp-1", null, "umbral_stock"))
                .thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        var result = newService().setParametro("emp-1", new SetConfigRequest("umbral_stock", "200", null));

        assertThat(result.valor()).isEqualTo("200");
        verify(repository).save(existente);
    }

    @Test
    void createsSeparateConfigPerProductoInsteadOfCollidingOnSameParametro() {
        when(repository.findByEmpresaIdAndProductoIdAndParametro("emp-1", "prod-A", "umbral_stock"))
                .thenReturn(Optional.empty());
        when(repository.save(org.mockito.ArgumentMatchers.any(ConfigSistema.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = newService().setParametro("emp-1", new SetConfigRequest("umbral_stock", "100", "prod-A"));

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

        var result = newService().setParametro("emp-1", new SetConfigRequest("umbral_stock", "200", "prod-A"));

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

        var result = newService().getConfig("emp-1");

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

        assertThat(newService().getConfig("emp-1")).isEmpty();
    }

    @Test
    void chequeaElUmbralDeDemandaAlInstanteCuandoSeGuardaConProducto() {
        when(repository.findByEmpresaIdAndProductoIdAndParametro(
                "emp-1", "prod-A", NotificacionesService.PARAMETRO_UMBRAL_DEMANDA))
                .thenReturn(Optional.empty());
        when(repository.save(any(ConfigSistema.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        newService().setParametro("emp-1",
                new SetConfigRequest(NotificacionesService.PARAMETRO_UMBRAL_DEMANDA, "200", "prod-A"));

        ArgumentCaptor<ConfigSistema> captor = ArgumentCaptor.forClass(ConfigSistema.class);
        verify(notificacionesService).chequearUmbralInmediato(captor.capture());
        assertThat(captor.getValue().getProductoId()).isEqualTo("prod-A");
        assertThat(captor.getValue().getValor()).isEqualTo("200");
    }

    @Test
    void noChequeaAlInstanteParametrosQueNoSonUmbralDeDemanda() {
        when(repository.findByEmpresaIdAndProductoIdAndParametro("emp-1", "prod-A", "umbral_stock"))
                .thenReturn(Optional.empty());
        when(repository.save(any(ConfigSistema.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        newService().setParametro("emp-1", new SetConfigRequest("umbral_stock", "100", "prod-A"));

        verify(notificacionesService, never()).chequearUmbralInmediato(any());
    }

    @Test
    void noChequeaAlInstanteUnUmbralDeDemandaSinProductoAsociado() {
        when(repository.findByEmpresaIdAndProductoIdAndParametro(
                "emp-1", null, NotificacionesService.PARAMETRO_UMBRAL_DEMANDA))
                .thenReturn(Optional.empty());
        when(repository.save(any(ConfigSistema.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        newService().setParametro("emp-1",
                new SetConfigRequest(NotificacionesService.PARAMETRO_UMBRAL_DEMANDA, "200", null));

        verify(notificacionesService, never()).chequearUmbralInmediato(any());
    }
}
