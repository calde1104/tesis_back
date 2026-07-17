package back.tesis.dto.config;

public record ConfigResponseDto(
        Integer id,
        String empresaId,
        String productoId,
        String parametro,
        String valor
) {
}
