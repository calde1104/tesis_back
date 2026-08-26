package back.tesis.modules.ingestion;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

final class VentasExcelParser {

    private VentasExcelParser() {
    }

    static ParseResult parse(byte[] contenido) {
        List<VentaRow> filas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(contenido))) {
            if (workbook.getNumberOfSheets() == 0) {
                errores.add("El archivo esta vacio");
                return new ParseResult(filas, errores);
            }
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            if (lastRow < 0) {
                errores.add("El archivo esta vacio");
                return new ParseResult(filas, errores);
            }

            int lineNumber = 1;
            for (int rowIndex = 1; rowIndex <= lastRow; rowIndex++) {
                lineNumber++;
                Row row = sheet.getRow(rowIndex);
                if (row == null || esFilaVacia(row)) {
                    continue;
                }
                try {
                    filas.add(VentaFilaParser.parseColumnas(columnasDe(row), lineNumber));
                } catch (VentaFilaParser.LineaInvalidaException e) {
                    errores.add(e.getMessage());
                }
            }
        } catch (IOException | RuntimeException e) {
            errores.add("No se pudo leer el archivo Excel: " + e.getMessage());
        }

        return new ParseResult(filas, errores);
    }

    private static boolean esFilaVacia(Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !cellToString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static String[] columnasDe(Row row) {
        int columnas = Math.max(row.getLastCellNum(), 5);
        String[] valores = new String[columnas];
        for (int i = 0; i < columnas; i++) {
            valores[i] = cellToString(row.getCell(i));
        }
        return valores;
    }

    private static String cellToString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> numericToPlainString(cell.getNumericCellValue());
            case FORMULA -> cellToStringFormula(cell);
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private static String cellToStringFormula(Cell cell) {
        return switch (cell.getCachedFormulaResultType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> numericToPlainString(cell.getNumericCellValue());
            default -> "";
        };
    }

    private static String numericToPlainString(double valor) {
        BigDecimal decimal = BigDecimal.valueOf(valor);
        if (decimal.stripTrailingZeros().scale() <= 0) {
            return decimal.stripTrailingZeros().toBigInteger().toString();
        }
        return decimal.stripTrailingZeros().toPlainString();
    }
}
