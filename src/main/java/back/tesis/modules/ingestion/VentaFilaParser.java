package back.tesis.modules.ingestion;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class VentaFilaParser {

    private static final int COLUMNAS_MINIMAS = 5;

    private VentaFilaParser() {
    }

    static VentaRow parseColumnas(String[] columnas, int lineNumber) {
        if (columnas.length < COLUMNAS_MINIMAS) {
            throw new LineaInvalidaException("Linea " + lineNumber + ": se esperaban al menos "
                    + COLUMNAS_MINIMAS + " columnas (sku,anio,mes,unidades_vendidas,monto_facturado)");
        }

        String sku = columnas[0].trim();
        if (sku.isEmpty()) {
            throw new LineaInvalidaException("Linea " + lineNumber + ": sku vacio");
        }

        try {
            int anio = Integer.parseInt(columnas[1].trim());
            int periodo = Integer.parseInt(columnas[2].trim());
            int unidadesVendidas = Integer.parseInt(columnas[3].trim());
            BigDecimal montoFacturado = new BigDecimal(columnas[4].trim());
            BigDecimal precioUnitario = (columnas.length > 5 && !columnas[5].isBlank())
                    ? new BigDecimal(columnas[5].trim())
                    : (unidadesVendidas > 0
                        ? montoFacturado.divide(BigDecimal.valueOf(unidadesVendidas), 2, RoundingMode.HALF_UP)
                        : null);
            return new VentaRow(lineNumber, sku, anio, periodo, unidadesVendidas, montoFacturado, precioUnitario);
        } catch (NumberFormatException e) {
            throw new LineaInvalidaException("Linea " + lineNumber + ": valor numerico invalido (" + e.getMessage() + ")");
        }
    }

    static final class LineaInvalidaException extends RuntimeException {
        LineaInvalidaException(String message) {
            super(message);
        }
    }
}
