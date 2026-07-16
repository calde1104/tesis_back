package back.tesis.dto.marketshare;

public record PriceGapDto(
        String productoId,
        Double precioUnitario,
        Double priceGapPct,
        int anio,
        int mes
) {
}
