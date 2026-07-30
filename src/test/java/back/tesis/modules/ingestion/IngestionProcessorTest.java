package back.tesis.modules.ingestion;

import back.tesis.entities.ArchivoIngesta;
import back.tesis.entities.Producto;
import back.tesis.entities.VentaMensual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
