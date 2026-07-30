package back.tesis.modules.forecast;

import back.tesis.dto.forecast.ForecastAgregadoRow;
import back.tesis.dto.forecast.SimulateRequest;
import back.tesis.dto.forecast.SimulateResponseDto;
import back.tesis.entities.ModeloVersion;
import back.tesis.modules.models.ModeloVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
    void getForecastReturnsTwelveMonthsHorizonAllNullWithoutModeloActivo() {
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1")).thenReturn(Optional.empty());

        ForecastService service = new ForecastService(mlForecastClient, prediccionRepository, modeloVersionRepository);

        var result = service.getForecast("emp-1");

        assertThat(result).hasSize(12);
        assertThat(result).allSatisfy(p -> assertThat(p.unidadesPredichas()).isNull());
        verifyNoInteractions(prediccionRepository);
    }

    @Test
    void getForecastFillsMonthsWithPrediccionesRealesAndLeavesRestNull() throws Exception {
        ModeloVersion modeloActivo = newModeloVersion("modelo-1");
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1")).thenReturn(Optional.of(modeloActivo));

        LocalDate primerMes = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        ForecastAgregadoRow puntoConDatos = new ForecastAgregadoRow(
                primerMes.getYear(), primerMes.getMonthValue(), 120.0, 100.0, 140.0);
        when(prediccionRepository.findForecastAgregado(anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(puntoConDatos));

        ForecastService service = new ForecastService(mlForecastClient, prediccionRepository, modeloVersionRepository);

        var result = service.getForecast("emp-1");

        assertThat(result).hasSize(12);
        assertThat(result.get(0).unidadesPredichas()).isEqualTo(120f);
        assertThat(result.subList(1, 12)).allSatisfy(p -> assertThat(p.unidadesPredichas()).isNull());
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
