package back.tesis.modules.ingestion;

import java.math.BigDecimal;

record VentaRow(int lineNumber, String sku, int anio, int periodo,
                 int unidadesVendidas, BigDecimal montoFacturado, BigDecimal precioUnitario) {
}
