package back.tesis.modules.forecast;

import back.tesis.dto.forecast.FeatureImportanceDto;
import back.tesis.dto.forecast.ForecastPointDto;
import back.tesis.dto.forecast.SimulateRequest;
import back.tesis.dto.forecast.SimulateResponseDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/{empresaId}/forecast")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping
    public List<ForecastPointDto> getForecast(@PathVariable String empresaId) {
        return forecastService.getForecast(empresaId);
    }

    @PostMapping("/simulate")
    public SimulateResponseDto simulate(@PathVariable String empresaId, @Valid @RequestBody SimulateRequest request) {
        return forecastService.simulate(empresaId, request);
    }

    @GetMapping("/feature-importance")
    public List<FeatureImportanceDto> getFeatureImportance(@PathVariable String empresaId) {
        return forecastService.getFeatureImportance(empresaId);
    }
}
