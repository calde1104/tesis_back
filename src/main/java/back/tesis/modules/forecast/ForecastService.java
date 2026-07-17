package back.tesis.modules.forecast;

import back.tesis.dto.forecast.FeatureImportanceDto;
import back.tesis.dto.forecast.ForecastPointDto;
import back.tesis.dto.forecast.SimulateRequest;
import back.tesis.dto.forecast.SimulateResponseDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ForecastService {

    private static final int HORIZONTE_MESES = 12;

    private final MlForecastClient mlForecastClient;

    public ForecastService(MlForecastClient mlForecastClient) {
        this.mlForecastClient = mlForecastClient;
    }

    public List<ForecastPointDto> getForecast(String empresaId) {
        LocalDate inicio = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        return java.util.stream.IntStream.range(0, HORIZONTE_MESES)
                .mapToObj(inicio::plusMonths)
                .map(fecha -> new ForecastPointDto(fecha.getYear(), fecha.getMonthValue(), null, null, null))
                .toList();
    }

    public SimulateResponseDto simulate(String empresaId, SimulateRequest request) {
        return mlForecastClient.simulate(empresaId, request);
    }

    public List<FeatureImportanceDto> getFeatureImportance(String empresaId) {
        return List.of();
    }
}
