package back.tesis.dto.dashboard;

public record ShareOfAssortmentDto(
        String empresaId,
        int anio,
        int mes,
        Double shareOfAssortmentPct
) {
}
