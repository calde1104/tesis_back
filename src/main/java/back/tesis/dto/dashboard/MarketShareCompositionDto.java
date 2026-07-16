package back.tesis.dto.dashboard;

public record MarketShareCompositionDto(
        String empresaId,
        String empresaNombre,
        Double marketShareUnidadesPromedio
) {
}
