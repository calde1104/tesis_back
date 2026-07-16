package back.tesis.dto.context;

public record ClimaDto(
        int anio,
        int mes,
        Float tempMaxPromedio,
        Float tempMediaPromedio,
        Float precipitacionMm
) {
}
