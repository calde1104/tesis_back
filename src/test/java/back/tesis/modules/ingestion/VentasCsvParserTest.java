package back.tesis.modules.ingestion;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class VentasCsvParserTest {

    @Test
    void parsesValidRowsIncludingComputedPrecioUnitario() {
        String csv = """
                sku,anio,mes,unidades_vendidas,monto_facturado
                SKU-1,2024,3,10,1000.00
                """;

        VentasCsvParser.ParseResult result = VentasCsvParser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertThat(result.errores()).isEmpty();
        assertThat(result.filas()).hasSize(1);
        VentasCsvParser.VentaRow row = result.filas().get(0);
        assertThat(row.sku()).isEqualTo("SKU-1");
        assertThat(row.anio()).isEqualTo(2024);
        assertThat(row.periodo()).isEqualTo(3);
        assertThat(row.unidadesVendidas()).isEqualTo(10);
        assertThat(row.montoFacturado()).isEqualByComparingTo("1000.00");
        assertThat(row.precioUnitario()).isEqualByComparingTo("100.00");
    }

    @Test
    void usesExplicitPrecioUnitarioWhenPresent() {
        String csv = """
                sku,anio,mes,unidades_vendidas,monto_facturado,precio_unitario
                SKU-1,2024,3,10,1000.00,99.99
                """;

        VentasCsvParser.ParseResult result = VentasCsvParser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertThat(result.filas().get(0).precioUnitario()).isEqualByComparingTo("99.99");
    }

    @Test
    void collectsErrorForInvalidNumericColumn() {
        String csv = """
                sku,anio,mes,unidades_vendidas,monto_facturado
                SKU-1,2024,marzo,10,1000.00
                """;

        VentasCsvParser.ParseResult result = VentasCsvParser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertThat(result.filas()).isEmpty();
        assertThat(result.errores()).hasSize(1);
        assertThat(result.errores().get(0)).contains("Linea 2");
    }

    @Test
    void collectsErrorForMissingColumns() {
        String csv = """
                sku,anio,mes,unidades_vendidas,monto_facturado
                SKU-1,2024,3
                """;

        VentasCsvParser.ParseResult result = VentasCsvParser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertThat(result.errores()).hasSize(1);
        assertThat(result.errores().get(0)).contains("columnas");
    }

    @Test
    void collectsErrorForEmptySku() {
        String csv = """
                sku,anio,mes,unidades_vendidas,monto_facturado
                ,2024,3,10,1000.00
                """;

        VentasCsvParser.ParseResult result = VentasCsvParser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertThat(result.errores()).hasSize(1);
        assertThat(result.errores().get(0)).contains("sku vacio");
    }

    @Test
    void emptyFileProducesError() {
        VentasCsvParser.ParseResult result = VentasCsvParser.parse(new byte[0]);

        assertThat(result.errores()).hasSize(1);
        assertThat(result.filas()).isEmpty();
    }

    @Test
    void ignoresBlankLines() {
        String csv = "sku,anio,mes,unidades_vendidas,monto_facturado\n\nSKU-1,2024,3,10,1000.00\n\n";

        VentasCsvParser.ParseResult result = VentasCsvParser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertThat(result.errores()).isEmpty();
        assertThat(result.filas()).hasSize(1);
    }

    @Test
    void zeroUnidadesVendidasLeavesPrecioUnitarioNullWithoutExplicitValue() {
        String csv = """
                sku,anio,mes,unidades_vendidas,monto_facturado
                SKU-1,2024,3,0,0.00
                """;

        VentasCsvParser.ParseResult result = VentasCsvParser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertThat(result.errores()).isEmpty();
        assertThat(result.filas().get(0).precioUnitario()).isNull();
    }
}
