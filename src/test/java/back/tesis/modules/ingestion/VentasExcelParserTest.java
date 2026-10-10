package back.tesis.modules.ingestion;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;

class VentasExcelParserTest {

    private byte[] workbookConFilas(String[] header, String[]... filas) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("ventas");
            escribirFila(sheet, 0, header);
            for (int i = 0; i < filas.length; i++) {
                escribirFila(sheet, i + 1, filas[i]);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void escribirFila(Sheet sheet, int rowIndex, String[] valores) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < valores.length; i++) {
            row.createCell(i).setCellValue(valores[i]);
        }
    }

    @Test
    void parsesValidRowsIncludingComputedPrecioUnitario() {
        byte[] xlsx = workbookConFilas(
                new String[]{"sku", "anio", "mes", "unidades_vendidas", "monto_facturado"},
                new String[]{"SKU-1", "2024", "3", "10", "1000.00"});

        ParseResult result = VentasExcelParser.parse(xlsx);

        assertThat(result.errores()).isEmpty();
        assertThat(result.filas()).hasSize(1);
        VentaRow row = result.filas().get(0);
        assertThat(row.sku()).isEqualTo("SKU-1");
        assertThat(row.anio()).isEqualTo(2024);
        assertThat(row.periodo()).isEqualTo(3);
        assertThat(row.unidadesVendidas()).isEqualTo(10);
        assertThat(row.montoFacturado()).isEqualByComparingTo("1000.00");
        assertThat(row.precioUnitario()).isEqualByComparingTo("100.00");
    }

    @Test
    void collectsErrorForInvalidNumericColumn() {
        byte[] xlsx = workbookConFilas(
                new String[]{"sku", "anio", "mes", "unidades_vendidas", "monto_facturado"},
                new String[]{"SKU-1", "2024", "marzo", "10", "1000.00"});

        ParseResult result = VentasExcelParser.parse(xlsx);

        assertThat(result.filas()).isEmpty();
        assertThat(result.errores()).hasSize(1);
        assertThat(result.errores().get(0)).contains("Linea 2");
    }

    @Test
    void ignoresBlankRows() {
        byte[] xlsx = workbookConFilas(
                new String[]{"sku", "anio", "mes", "unidades_vendidas", "monto_facturado"},
                new String[]{"", "", "", "", ""},
                new String[]{"SKU-1", "2024", "3", "10", "1000.00"});

        ParseResult result = VentasExcelParser.parse(xlsx);

        assertThat(result.errores()).isEmpty();
        assertThat(result.filas()).hasSize(1);
    }

    @Test
    void emptySheetProducesError() {
        byte[] xlsx;
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("ventas");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            xlsx = out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        ParseResult result = VentasExcelParser.parse(xlsx);

        assertThat(result.errores()).hasSize(1);
        assertThat(result.filas()).isEmpty();
    }

    @Test
    void workbookSinNingunaHojaProduceError() {
        byte[] xlsx;
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            xlsx = out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        ParseResult result = VentasExcelParser.parse(xlsx);

        assertThat(result.errores()).containsExactly("El archivo esta vacio");
        assertThat(result.filas()).isEmpty();
    }

    @Test
    void unArchivoIlegibleProduceUnErrorEnVezDeRomperElProceso() {
        byte[] basura = "esto no es un xlsx valido".getBytes();

        ParseResult result = VentasExcelParser.parse(basura);

        assertThat(result.filas()).isEmpty();
        assertThat(result.errores()).hasSize(1);
        assertThat(result.errores().get(0)).contains("No se pudo leer el archivo Excel");
    }

    @Test
    void leeColumnasNumericasConvirtiendolasAEnteroSinDecimalesSobrantes() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("ventas");
            escribirFila(sheet, 0, new String[]{"sku", "anio", "mes", "unidades_vendidas", "monto_facturado"});
            Row fila = sheet.createRow(1);
            fila.createCell(0).setCellValue("SKU-1");
            fila.createCell(1).setCellValue(2024);
            fila.createCell(2).setCellValue(3);
            fila.createCell(3).setCellValue(10);
            fila.createCell(4).setCellValue(1500.50);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            ParseResult result = VentasExcelParser.parse(out.toByteArray());

            assertThat(result.errores()).isEmpty();
            VentaRow row = result.filas().get(0);
            assertThat(row.anio()).isEqualTo(2024);
            assertThat(row.periodo()).isEqualTo(3);
            assertThat(row.montoFacturado()).isEqualByComparingTo("1500.50");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void leeCeldasConFormulaUsandoElResultadoCacheado() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("ventas");
            escribirFila(sheet, 0, new String[]{"sku", "anio", "mes", "unidades_vendidas", "monto_facturado"});
            Row fila = sheet.createRow(1);
            fila.createCell(0).setCellValue("SKU-1");
            fila.createCell(1).setCellValue(2024);
            fila.createCell(2).setCellValue(3);
            Cell celdaFormula = fila.createCell(3);
            celdaFormula.setCellFormula("5+5");
            celdaFormula.setCellValue(10.0);
            fila.createCell(4).setCellValue(1000.0);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            ParseResult result = VentasExcelParser.parse(out.toByteArray());

            assertThat(result.errores()).isEmpty();
            assertThat(result.filas().get(0).unidadesVendidas()).isEqualTo(10);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
