package back.tesis.modules.ingestion;

import back.tesis.entities.ArchivoIngesta;
import back.tesis.entities.Producto;
import back.tesis.entities.VentaMensual;
import back.tesis.entities.VentaSemanal;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionProcessorTest {

    @Mock
    private ArchivoIngestaRepository archivoRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private VentaCountRepository ventaMensualRepository;
    @Mock
    private VentaSemanalCountRepository ventaSemanalRepository;

    private IngestionProcessor newProcessor() {
        return new IngestionProcessor(archivoRepository, productoRepository, ventaMensualRepository, ventaSemanalRepository);
    }

    @Test
    void marcaArchivoOkYCargaVentaCuandoSkuMatcheaUnSoloProducto() {
        ArchivoIngesta archivo = archivoIngesta();
        when(archivoRepository.findById("arch-1")).thenReturn(Optional.of(archivo));
        when(productoRepository.findBySku("SKU-1")).thenReturn(List.of(producto("prod-1")));
        when(ventaMensualRepository.existsByProductoIdAndEmpresaIdAndAnioAndMesAndArchivoId(
                "prod-1", "emp-1", 2024, 3, "arch-1")).thenReturn(false);
        when(ventaMensualRepository.existsByProductoIdAndEmpresaIdAndAnioAndMes(
                "prod-1", "emp-1", 2024, 3)).thenReturn(false);

        String csv = "sku,anio,mes,unidades_vendidas,monto_facturado\nSKU-1,2024,3,10,1000.00\n";

        newProcessor().procesar("arch-1", "emp-1", "MENSUAL", csv.getBytes(StandardCharsets.UTF_8));

        verify(ventaMensualRepository).save(any(VentaMensual.class));
        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_OK);
    }

    @Test
    void marcaArchivoErrorYNoInsertaCuandoSkuNoExiste() {
        ArchivoIngesta archivo = archivoIngesta();
        when(archivoRepository.findById("arch-1")).thenReturn(Optional.of(archivo));
        when(productoRepository.findBySku("SKU-X")).thenReturn(List.of());

        String csv = "sku,anio,mes,unidades_vendidas,monto_facturado\nSKU-X,2024,3,10,1000.00\n";

        newProcessor().procesar("arch-1", "emp-1", "MENSUAL", csv.getBytes(StandardCharsets.UTF_8));

        verify(ventaMensualRepository, never()).save(any());
        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_ERROR);
    }

    @Test
    void marcaArchivoErrorYNoInsertaCuandoSkuEsAmbiguo() {
        ArchivoIngesta archivo = archivoIngesta();
        when(archivoRepository.findById("arch-1")).thenReturn(Optional.of(archivo));
        when(productoRepository.findBySku("SKU-1")).thenReturn(List.of(producto("prod-1"), producto("prod-2")));

        String csv = "sku,anio,mes,unidades_vendidas,monto_facturado\nSKU-1,2024,3,10,1000.00\n";

        newProcessor().procesar("arch-1", "emp-1", "MENSUAL", csv.getBytes(StandardCharsets.UTF_8));

        verify(ventaMensualRepository, never()).save(any());
        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_ERROR);
    }

    @Test
    void rechazaFilaSiYaExisteVentaCargadaPorOtroArchivoSinPisarla() {
        ArchivoIngesta archivo = archivoIngesta();
        when(archivoRepository.findById("arch-1")).thenReturn(Optional.of(archivo));
        when(productoRepository.findBySku("SKU-1")).thenReturn(List.of(producto("prod-1")));
        when(ventaMensualRepository.existsByProductoIdAndEmpresaIdAndAnioAndMesAndArchivoId(
                "prod-1", "emp-1", 2024, 3, "arch-1")).thenReturn(false);
        when(ventaMensualRepository.existsByProductoIdAndEmpresaIdAndAnioAndMes(
                "prod-1", "emp-1", 2024, 3)).thenReturn(true);

        String csv = "sku,anio,mes,unidades_vendidas,monto_facturado\nSKU-1,2024,3,10,1000.00\n";

        newProcessor().procesar("arch-1", "emp-1", "MENSUAL", csv.getBytes(StandardCharsets.UTF_8));

        verify(ventaMensualRepository, never()).save(any());
        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_ERROR);
    }

    @Test
    void resyncEsIdempotenteParaFilasYaCargadasPorElMismoArchivo() {
        ArchivoIngesta archivo = archivoIngesta();
        when(archivoRepository.findById("arch-1")).thenReturn(Optional.of(archivo));
        when(productoRepository.findBySku("SKU-1")).thenReturn(List.of(producto("prod-1")));
        when(ventaMensualRepository.existsByProductoIdAndEmpresaIdAndAnioAndMesAndArchivoId(
                "prod-1", "emp-1", 2024, 3, "arch-1")).thenReturn(true);

        String csv = "sku,anio,mes,unidades_vendidas,monto_facturado\nSKU-1,2024,3,10,1000.00\n";

        newProcessor().procesar("arch-1", "emp-1", "MENSUAL", csv.getBytes(StandardCharsets.UTF_8));

        verify(ventaMensualRepository, never()).save(any());
        verify(ventaMensualRepository, times(0)).existsByProductoIdAndEmpresaIdAndAnioAndMes(any(), any(), eq(2024), eq(3));
        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_OK);
    }

    @Test
    void noHaceNadaSiElArchivoYaNoExiste() {
        when(archivoRepository.findById("arch-ausente")).thenReturn(Optional.empty());

        newProcessor().procesar("arch-ausente", "emp-1", "MENSUAL", new byte[0]);

        verify(archivoRepository, never()).save(any());
        verify(productoRepository, never()).findBySku(any());
    }

    @Test
    void procesaUnArchivoXlsxDelegandoAlParserDeExcel() throws IOException {
        ArchivoIngesta archivo = new ArchivoIngesta("emp-1", "ventas.xlsx", "hash1", "MENSUAL",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        when(archivoRepository.findById("arch-1")).thenReturn(Optional.of(archivo));
        when(productoRepository.findBySku("SKU-1")).thenReturn(List.of(producto("prod-1")));
        when(ventaMensualRepository.existsByProductoIdAndEmpresaIdAndAnioAndMesAndArchivoId(
                "prod-1", "emp-1", 2024, 3, "arch-1")).thenReturn(false);
        when(ventaMensualRepository.existsByProductoIdAndEmpresaIdAndAnioAndMes(
                "prod-1", "emp-1", 2024, 3)).thenReturn(false);

        byte[] xlsx = workbookConFilas(
                new String[]{"sku", "anio", "mes", "unidades_vendidas", "monto_facturado"},
                new String[]{"SKU-1", "2024", "3", "10", "1000.00"});

        newProcessor().procesar("arch-1", "emp-1", "MENSUAL", xlsx);

        verify(ventaMensualRepository).save(any(VentaMensual.class));
        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_OK);
    }

    @Test
    void cargaVentaSemanalCuandoLaGranularidadEsSemanal() {
        ArchivoIngesta archivo = archivoIngesta();
        when(archivoRepository.findById("arch-1")).thenReturn(Optional.of(archivo));
        when(productoRepository.findBySku("SKU-1")).thenReturn(List.of(producto("prod-1")));
        when(ventaSemanalRepository.existsByProductoIdAndEmpresaIdAndAnioAndSemanaAndArchivoId(
                "prod-1", "emp-1", 2024, 3, "arch-1")).thenReturn(false);
        when(ventaSemanalRepository.existsByProductoIdAndEmpresaIdAndAnioAndSemana(
                "prod-1", "emp-1", 2024, 3)).thenReturn(false);

        String csv = "sku,anio,semana,unidades_vendidas,monto_facturado\nSKU-1,2024,3,10,1000.00\n";

        newProcessor().procesar("arch-1", "emp-1", "SEMANAL", csv.getBytes(StandardCharsets.UTF_8));

        ArgumentCaptor<VentaSemanal> captor = ArgumentCaptor.forClass(VentaSemanal.class);
        verify(ventaSemanalRepository).save(captor.capture());
        assertThat(captor.getValue().getSemana()).isEqualTo(3);
        assertThat(captor.getValue().getUnidadesVendidas()).isEqualTo(10);
        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_OK);
    }

    @Test
    void rechazaFilaSemanalSiYaExisteCargadaPorOtroArchivoSinPisarla() {
        ArchivoIngesta archivo = archivoIngesta();
        when(archivoRepository.findById("arch-1")).thenReturn(Optional.of(archivo));
        when(productoRepository.findBySku("SKU-1")).thenReturn(List.of(producto("prod-1")));
        when(ventaSemanalRepository.existsByProductoIdAndEmpresaIdAndAnioAndSemanaAndArchivoId(
                "prod-1", "emp-1", 2024, 3, "arch-1")).thenReturn(false);
        when(ventaSemanalRepository.existsByProductoIdAndEmpresaIdAndAnioAndSemana(
                "prod-1", "emp-1", 2024, 3)).thenReturn(true);

        String csv = "sku,anio,semana,unidades_vendidas,monto_facturado\nSKU-1,2024,3,10,1000.00\n";

        newProcessor().procesar("arch-1", "emp-1", "SEMANAL", csv.getBytes(StandardCharsets.UTF_8));

        verify(ventaSemanalRepository, never()).save(any());
        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_ERROR);
    }

    @Test
    void unaFilaRechazadaPorLaBaseNoAbortaElRestoDelArchivo() {
        ArchivoIngesta archivo = archivoIngesta();
        when(archivoRepository.findById("arch-1")).thenReturn(Optional.of(archivo));
        when(productoRepository.findBySku("SKU-1")).thenReturn(List.of(producto("prod-1")));
        when(productoRepository.findBySku("SKU-2")).thenReturn(List.of(producto("prod-2")));
        when(ventaMensualRepository.existsByProductoIdAndEmpresaIdAndAnioAndMesAndArchivoId(
                any(), any(), eq(2024), eq(3), eq("arch-1"))).thenReturn(false);
        when(ventaMensualRepository.existsByProductoIdAndEmpresaIdAndAnioAndMes(
                "prod-1", "emp-1", 2024, 3)).thenReturn(false);
        when(ventaMensualRepository.existsByProductoIdAndEmpresaIdAndAnioAndMes(
                "prod-2", "emp-1", 2024, 3)).thenReturn(false);
        when(ventaMensualRepository.save(argThatProductoId("prod-1")))
                .thenThrow(new DataIntegrityViolationException("CHECK violado", new RuntimeException("unidades negativas")));

        String csv = "sku,anio,mes,unidades_vendidas,monto_facturado\n"
                + "SKU-1,2024,3,-5,1000.00\n"
                + "SKU-2,2024,3,10,1000.00\n";

        newProcessor().procesar("arch-1", "emp-1", "MENSUAL", csv.getBytes(StandardCharsets.UTF_8));

        verify(ventaMensualRepository).save(argThatProductoId("prod-2"));
        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_ERROR);
    }

    @Test
    void unaFallaInesperadaNoDejaElArchivoColgadoEnProcesando() {
        ArchivoIngesta archivo = archivoIngesta();
        when(archivoRepository.findById("arch-1")).thenReturn(Optional.of(archivo));
        when(productoRepository.findBySku(any())).thenThrow(new IllegalStateException("catalogo no disponible"));

        String csv = "sku,anio,mes,unidades_vendidas,monto_facturado\nSKU-1,2024,3,10,1000.00\n";

        newProcessor().procesar("arch-1", "emp-1", "MENSUAL", csv.getBytes(StandardCharsets.UTF_8));

        assertThat(archivo.getEstado()).isEqualTo(ArchivoIngesta.ESTADO_ERROR);
        verify(archivoRepository, times(2)).save(archivo);
    }

    private static VentaMensual argThatProductoId(String productoId) {
        return org.mockito.ArgumentMatchers.argThat(v -> v != null && productoId.equals(v.getProductoId()));
    }

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

    private static ArchivoIngesta archivoIngesta() {
        return new ArchivoIngesta("emp-1", "ventas.csv", "hash1", "MENSUAL",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    }

    private static Producto producto(String id) {
        try {
            var constructor = Producto.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Producto producto = constructor.newInstance();
            Field idField = producto.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(producto, id);
            return producto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
