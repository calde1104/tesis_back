package back.tesis.modules.forecast;

import back.tesis.dto.forecast.ForecastAgregadoRow;
import back.tesis.dto.forecast.ForecastPointDto;
import back.tesis.dto.forecast.ModelExplainabilityDto;
import back.tesis.dto.forecast.SimulateRequest;
import back.tesis.dto.forecast.SimulateResponseDto;
import back.tesis.entities.ModeloVersion;
import back.tesis.modules.models.ModeloVersionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ForecastService {

    private final MlForecastClient mlForecastClient;
    private final PrediccionRepository prediccionRepository;
    private final ModeloVersionRepository modeloVersionRepository;

    public ForecastService(MlForecastClient mlForecastClient, PrediccionRepository prediccionRepository,
            ModeloVersionRepository modeloVersionRepository) {
        this.mlForecastClient = mlForecastClient;
        this.prediccionRepository = prediccionRepository;
        this.modeloVersionRepository = modeloVersionRepository;
    }

    public List<ForecastPointDto> getForecast(String empresaId, String sku) {
        Optional<ModeloVersion> modeloActivo = modeloVersionRepository.findByEmpresaIdAndActivoTrue(empresaId);
        String skuNormalizado = normalizarSku(sku);

        return modeloActivo
                .map(modelo -> prediccionRepository.findForecastAgregado(empresaId, modelo.getId(), skuNormalizado))
                .orElse(List.of())
                .stream()
                .map(ForecastService::toForecastPointDto)
                .toList();
    }

    private static String normalizarSku(String sku) {
        return (sku == null || sku.isBlank() || "total".equalsIgnoreCase(sku)) ? null : sku;
    }

    private static ForecastPointDto toForecastPointDto(ForecastAgregadoRow row) {
        return new ForecastPointDto(row.anio(), row.mes(),
                toFloat(row.unidadesPredichas()), toFloat(row.intervaloInf()), toFloat(row.intervaloSup()));
    }

    private static Float toFloat(Double value) {
        return value == null ? null : value.floatValue();
    }

    public SimulateResponseDto simulate(String empresaId, SimulateRequest request) {
        return mlForecastClient.simulate(empresaId, request);
    }

    public ModelExplainabilityDto getFeatureImportance(String empresaId) {
        return mlForecastClient.getModelExplainability();
    }
}
