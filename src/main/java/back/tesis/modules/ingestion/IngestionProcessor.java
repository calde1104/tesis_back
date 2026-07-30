package back.tesis.modules.ingestion;

import back.tesis.entities.ArchivoIngesta;
import back.tesis.entities.Producto;
import back.tesis.entities.VentaMensual;
import back.tesis.entities.VentaSemanal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class IngestionProcessor {

    private static final Logger log = LoggerFactory.getLogger(IngestionProcessor.class);
    private static final String GRANULARIDAD_SEMANAL = "SEMANAL";

    private final ArchivoIngestaRepository archivoRepository;
    private final ProductoRepository productoRepository;
    private final VentaCountRepository ventaMensualRepository;
    private final VentaSemanalCountRepository ventaSemanalRepository;

    public IngestionProcessor(
            ArchivoIngestaRepository archivoRepository,
            ProductoRepository productoRepository,
            VentaCountRepository ventaMensualRepository,
            VentaSemanalCountRepository ventaSemanalRepository
    ) {
        this.archivoRepository = archivoRepository;
        this.productoRepository = productoRepository;
        this.ventaMensualRepository = ventaMensualRepository;
        this.ventaSemanalRepository = ventaSemanalRepository;
    }

    @Async
    public void procesar(String archivoId, String empresaId, String granularidad, byte[] contenido) {
        Optional<ArchivoIngesta> archivoOpt = archivoRepository.findById(archivoId);
        if (archivoOpt.isEmpty()) {
            log.warn("Archivo de ingesta {} desaparecio antes de poder procesarse", archivoId);
            return;
        }
        ArchivoIngesta archivo = archivoOpt.get();
        archivo.setEstado(ArchivoIngesta.ESTADO_PROCESANDO);
        archivoRepository.save(archivo);

        List<String> errores = new ArrayList<>();
        int cargadas = 0;
        try {
            VentasCsvParser.ParseResult parsed = VentasCsvParser.parse(contenido);
            errores.addAll(parsed.errores());

            boolean semanal = GRANULARIDAD_SEMANAL.equalsIgnoreCase(granularidad);
            for (VentasCsvParser.VentaRow fila : parsed.filas()) {
                List<Producto> productos = productoRepository.findBySku(fila.sku());
                if (productos.size() != 1) {
                    errores.add("Linea " + fila.lineNumber() + ": SKU '" + fila.sku() + "' "
                            + (productos.isEmpty() ? "no encontrado en el catalogo" : "ambiguo (existe en mas de una marca)"));
                    continue;
                }
                String productoId = productos.get(0).getId();

                try {
                    cargadas += semanal
                            ? cargarVentaSemanal(archivoId, empresaId, productoId, fila, errores)
                            : cargarVentaMensual(archivoId, empresaId, productoId, fila, errores);
                } catch (DataAccessException e) {
                    errores.add("Linea " + fila.lineNumber() + ": rechazada por la base de datos ("
                            + rootMessage(e) + ")");
                }
            }
        } catch (RuntimeException e) {
            log.error("Fallo inesperado procesando archivo de ingesta {}", archivoId, e);
            errores.add("Error inesperado durante el procesamiento: " + rootMessage(e));
        }

        archivo.setEstado(errores.isEmpty() ? ArchivoIngesta.ESTADO_OK : ArchivoIngesta.ESTADO_ERROR);
        archivoRepository.save(archivo);

        if (errores.isEmpty()) {
            log.info("Archivo de ingesta {} procesado OK: {} filas cargadas", archivoId, cargadas);
        } else {
            log.warn("Archivo de ingesta {} procesado con errores ({} filas cargadas, {} errores): {}",
                    archivoId, cargadas, errores.size(), errores);
        }
    }

    private static String rootMessage(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }

    private int cargarVentaMensual(String archivoId, String empresaId, String productoId,
                                    VentasCsvParser.VentaRow fila, List<String> errores) {
        if (ventaMensualRepository.existsByProductoIdAndEmpresaIdAndAnioAndMesAndArchivoId(
                productoId, empresaId, fila.anio(), fila.periodo(), archivoId)) {
            return 1;
        }
        if (ventaMensualRepository.existsByProductoIdAndEmpresaIdAndAnioAndMes(
                productoId, empresaId, fila.anio(), fila.periodo())) {
            errores.add("Linea " + fila.lineNumber() + ": ya existe una venta cargada para ese producto/periodo "
                    + "(por otro archivo) - no se sobrescribe");
            return 0;
        }
        ventaMensualRepository.save(new VentaMensual(productoId, archivoId, empresaId,
                fila.anio(), fila.periodo(), fila.unidadesVendidas(), fila.montoFacturado(), fila.precioUnitario()));
        return 1;
    }

    private int cargarVentaSemanal(String archivoId, String empresaId, String productoId,
                                    VentasCsvParser.VentaRow fila, List<String> errores) {
        if (ventaSemanalRepository.existsByProductoIdAndEmpresaIdAndAnioAndSemanaAndArchivoId(
                productoId, empresaId, fila.anio(), fila.periodo(), archivoId)) {
            return 1;
        }
        if (ventaSemanalRepository.existsByProductoIdAndEmpresaIdAndAnioAndSemana(
                productoId, empresaId, fila.anio(), fila.periodo())) {
            errores.add("Linea " + fila.lineNumber() + ": ya existe una venta cargada para ese producto/periodo "
                    + "(por otro archivo) - no se sobrescribe");
            return 0;
        }
        ventaSemanalRepository.save(new VentaSemanal(productoId, archivoId, empresaId,
                fila.anio(), fila.periodo(), fila.unidadesVendidas(), fila.montoFacturado()));
        return 1;
    }
}
