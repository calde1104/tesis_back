package back.tesis.modules.forecast;

import back.tesis.dto.forecast.FeatureImportanceDto;
import back.tesis.dto.forecast.ForecastAgregadoRow;
import back.tesis.dto.forecast.ForecastPointDto;
import back.tesis.dto.forecast.SimulateRequest;
import back.tesis.dto.forecast.SimulateResponseDto;
import back.tesis.entities.ModeloVersion;
import back.tesis.modules.models.ModeloVersionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ForecastService {

    private static final int HORIZONTE_MESES = 12;

    private final MlForecastClient mlForecastClient;
    private final PrediccionRepository prediccionRepository;
    private final ModeloVersionRepository modeloVersionRepository;

    public ForecastService(MlForecastClient mlForecastClient, PrediccionRepository prediccionRepository,
            ModeloVersionRepository modeloVersionRepository) {
        this.mlForecastClient = mlForecastClient;
        this.prediccionRepository = prediccionRepository;
        this.modeloVersionRepository = modeloVersionRepository;
    }

    public List<ForecastPointDto> getForecast(String empresaId) {
        LocalDate inicio = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        LocalDate fin = inicio.plusMonths(HORIZONTE_MESES - 1L);

        Optional<ModeloVersion> modeloActivo = modeloVersionRepository.findByEmpresaIdAndActivoTrue(empresaId);

        Map<String, ForecastAgregadoRow> porPeriodo = modeloActivo
                .map(modelo -> prediccionRepository.findForecastAgregado(empresaId, modelo.getId(),
                        inicio.getYear(), inicio.getMonthValue(), fin.getYear(), fin.getMonthValue()))
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(row -> clavePeriodo(row.anio(), row.mes()), Function.identity()));

        return IntStream.range(0, HORIZONTE_MESES)
                .mapToObj(inicio::plusMonths)
                .map(fecha -> toForecastPointDto(fecha,
                        porPeriodo.get(clavePeriodo(fecha.getYear(), fecha.getMonthValue()))))
                .toList();
    }

    private static ForecastPointDto toForecastPointDto(LocalDate fecha, ForecastAgregadoRow row) {
        if (row == null) {
            return new ForecastPointDto(fecha.getYear(), fecha.getMonthValue(), null, null, null);
        }
        return new ForecastPointDto(fecha.getYear(), fecha.getMonthValue(),
                toFloat(row.unidadesPredichas()), toFloat(row.intervaloInf()), toFloat(row.intervaloSup()));
    }

    private static Float toFloat(Double value) {
        return value == null ? null : value.floatValue();
    }

    private static String clavePeriodo(int anio, int mes) {
        return anio + "-" + mes;
    }

    public SimulateResponseDto simulate(String empresaId, SimulateRequest request) {
        return mlForecastClient.simulate(empresaId, request);
    }

    public List<FeatureImportanceDto> getFeatureImportance(String empresaId) {
        return List.of();
    }
}
