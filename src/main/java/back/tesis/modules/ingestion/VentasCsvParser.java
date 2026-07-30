package back.tesis.modules.ingestion;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class VentasCsvParser {

    private static final int COLUMNAS_MINIMAS = 5;

    private VentasCsvParser() {
    }

    record VentaRow(int lineNumber, String sku, int anio, int periodo,
                     int unidadesVendidas, BigDecimal montoFacturado, BigDecimal precioUnitario) {
    }

    record ParseResult(List<VentaRow> filas, List<String> errores) {
    }

    static ParseResult parse(byte[] contenido) {
        List<VentaRow> filas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(contenido), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                errores.add("El archivo esta vacio");
                return new ParseResult(filas, errores);
            }

            String linea;
            int lineNumber = 1;
            while ((linea = reader.readLine()) != null) {
                lineNumber++;
                if (linea.isBlank()) {
                    continue;
                }
                try {
                    filas.add(parseLinea(linea, lineNumber));
                } catch (LineaInvalidaException e) {
                    errores.add(e.getMessage());
                }
            }
        } catch (IOException e) {
            errores.add("No se pudo leer el archivo: " + e.getMessage());
        }

        return new ParseResult(filas, errores);
    }

    private static VentaRow parseLinea(String linea, int lineNumber) {
        String[] columnas = linea.split(",", -1);
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

    private static final class LineaInvalidaException extends RuntimeException {
        LineaInvalidaException(String message) {
            super(message);
        }
    }
}
