package back.tesis.modules.forecast;

import back.tesis.dto.forecast.ForecastAgregadoRow;
import back.tesis.dto.forecast.ModelExplainabilityDto;
import back.tesis.dto.forecast.SimulateRequest;
import back.tesis.dto.forecast.SimulateResponseDto;
import back.tesis.entities.ModeloVersion;
import back.tesis.modules.models.ModeloVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    @Mock
    private MlForecastClient mlForecastClient;

    @Mock
    private PrediccionRepository prediccionRepository;

    @Mock
    private ModeloVersionRepository modeloVersionRepository;

    @Test
    void getForecastReturnsEmptyWithoutModeloActivo() {
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1")).thenReturn(Optional.empty());

        ForecastService service = new ForecastService(mlForecastClient, prediccionRepository, modeloVersionRepository);

        var result = service.getForecast("emp-1", null);

        assertThat(result).isEmpty();
        verifyNoInteractions(prediccionRepository);
    }

    @Test
    void getForecastReturnsExactlyLoPredichoSinAcotarPorFechaDelServidor() throws Exception {
        ModeloVersion modeloActivo = newModeloVersion("modelo-1");
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1")).thenReturn(Optional.of(modeloActivo));

        ForecastAgregadoRow puntoPasado = new ForecastAgregadoRow(2024, 11, 360.0, 300.0, 420.0);
        ForecastAgregadoRow puntoFuturoLejano = new ForecastAgregadoRow(2026, 2, 5685.0, 4800.0, 6500.0);
        when(prediccionRepository.findForecastAgregado("emp-1", "modelo-1", null))
                .thenReturn(List.of(puntoPasado, puntoFuturoLejano));

        ForecastService service = new ForecastService(mlForecastClient, prediccionRepository, modeloVersionRepository);

        var result = service.getForecast("emp-1", null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).anio()).isEqualTo(2024);
        assertThat(result.get(0).mes()).isEqualTo(11);
        assertThat(result.get(0).unidadesPredichas()).isEqualTo(360f);
        assertThat(result.get(1).anio()).isEqualTo(2026);
        assertThat(result.get(1).mes()).isEqualTo(2);
        assertThat(result.get(1).unidadesPredichas()).isEqualTo(5685f);
    }

    @Test
    void getForecastNormalizesBlankAndTotalSkuToNull() throws Exception {
        ModeloVersion modeloActivo = newModeloVersion("modelo-1");
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1")).thenReturn(Optional.of(modeloActivo));
        when(prediccionRepository.findForecastAgregado("emp-1", "modelo-1", null)).thenReturn(List.of());

        ForecastService service = new ForecastService(mlForecastClient, prediccionRepository, modeloVersionRepository);

        service.getForecast("emp-1", "");
        service.getForecast("emp-1", "total");
        service.getForecast("emp-1", "TOTAL");

        verify(prediccionRepository, times(3))
                .findForecastAgregado("emp-1", "modelo-1", null);
    }

    @Test
    void getForecastPassesSkuThroughToRepositoryWhenGivenAParticularProduct() throws Exception {
        ModeloVersion modeloActivo = newModeloVersion("modelo-1");
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1")).thenReturn(Optional.of(modeloActivo));
        when(prediccionRepository.findForecastAgregado("emp-1", "modelo-1", "SKU-001")).thenReturn(List.of());

        ForecastService service = new ForecastService(mlForecastClient, prediccionRepository, modeloVersionRepository);

        service.getForecast("emp-1", "SKU-001");

        verify(prediccionRepository).findForecastAgregado("emp-1", "modelo-1", "SKU-001");
    }

    @Test
    void simulateDelegatesToMlClientSynchronously() {
        SimulateRequest request = new SimulateRequest("prod-1", 10.0, 5.0, 2.0);
        when(mlForecastClient.simulate("emp-1", request))
                .thenReturn(new SimulateResponseDto("success", "prod-1", null, List.of()));

        ForecastService service = new ForecastService(mlForecastClient, prediccionRepository, modeloVersionRepository);

        var result = service.simulate("emp-1", request);

        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void getFeatureImportanceDelegatesToMlClient() {
        ModelExplainabilityDto response = new ModelExplainabilityDto(
                "success", "LinearRegression", 11, java.util.Map.of("Historial y Tendencia", 55.2), List.of());
        when(mlForecastClient.getModelExplainability()).thenReturn(response);

        ForecastService service = new ForecastService(mlForecastClient, prediccionRepository, modeloVersionRepository);

        var result = service.getFeatureImportance("emp-1");

        assertThat(result.status()).isEqualTo("success");
        assertThat(result.modeloTipo()).isEqualTo("LinearRegression");
    }

    private static ModeloVersion newModeloVersion(String id) throws Exception {
        var constructor = ModeloVersion.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ModeloVersion modelo = constructor.newInstance();
        Field idField = modelo.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(modelo, id);
        return modelo;
    }
}
