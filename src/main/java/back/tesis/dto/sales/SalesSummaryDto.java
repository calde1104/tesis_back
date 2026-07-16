package back.tesis.dto.sales;

import java.math.BigDecimal;

public record SalesSummaryDto(
        long totalUnidades,
        BigDecimal totalFacturado
) {
}
