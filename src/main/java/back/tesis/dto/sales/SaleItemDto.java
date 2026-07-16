package back.tesis.dto.sales;

import java.math.BigDecimal;

public record SaleItemDto(
        String productoId,
        int anio,
        int mes,
        int unidadesVendidas,
        BigDecimal montoFacturado,
        BigDecimal precioUnitario
) {
}
