package back.tesis.dto.sales;

import java.math.BigDecimal;

public record MonthlyRevenueDto(int anio, int mes, BigDecimal montoFacturado, Long unidadesVendidas) {
}
