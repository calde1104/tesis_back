package back.tesis.dto.marketshare;

public record PriceGapDto(
        String productoId,
        String nombreProducto,
        Double precioUnitario,
        Double priceGapPct,
        int anio,
        int mes
) {
}
