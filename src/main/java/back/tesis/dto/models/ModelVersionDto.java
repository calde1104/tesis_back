package back.tesis.dto.models;

public record ModelVersionDto(
        String id,
        String version,
        float mae,
        float rmse,
        float mape,
        boolean activo
) {
}
