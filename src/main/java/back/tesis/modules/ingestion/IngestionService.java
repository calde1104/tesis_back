package back.tesis.modules.ingestion;

import back.tesis.common.exceptions.ConflictException;
import back.tesis.common.exceptions.InvalidFileException;
import back.tesis.common.exceptions.ResourceNotFoundException;
import back.tesis.dto.ingestion.IngestionHistoryItemDto;
import back.tesis.dto.ingestion.UploadResponseDto;
import back.tesis.entities.ArchivoIngesta;
import back.tesis.entities.AuditoriaIntegridad;
import back.tesis.mapper.ingestion.IngestionMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
public class IngestionService {

    private final ArchivoIngestaRepository repository;
    private final VentaCountRepository ventaMensualCountRepository;
    private final VentaSemanalCountRepository ventaSemanalCountRepository;
    private final AuditoriaIntegridadRepository auditoriaIntegridadRepository;
    private final IngestionFileStorage fileStorage;
    private final IngestionProcessor processor;

    public IngestionService(
            ArchivoIngestaRepository repository,
            VentaCountRepository ventaMensualCountRepository,
            VentaSemanalCountRepository ventaSemanalCountRepository,
            AuditoriaIntegridadRepository auditoriaIntegridadRepository,
            IngestionFileStorage fileStorage,
            IngestionProcessor processor
    ) {
        this.repository = repository;
        this.ventaMensualCountRepository = ventaMensualCountRepository;
        this.ventaSemanalCountRepository = ventaSemanalCountRepository;
        this.auditoriaIntegridadRepository = auditoriaIntegridadRepository;
        this.fileStorage = fileStorage;
        this.processor = processor;
    }

    public UploadResponseDto upload(String empresaId, MultipartFile file, String granularidad,
                                     LocalDate periodoDesde, LocalDate periodoHasta) {
        String nombreArchivo = validarNombreArchivo(file.getOriginalFilename());
        validarContentType(file.getContentType());
        byte[] contenido = readBytes(file);
        String hash = sha256(contenido);
        if (repository.existsByHashSha256(hash)) {
            throw new ConflictException("El archivo ya fue cargado previamente (hash duplicado)");
        }

        ArchivoIngesta archivo = new ArchivoIngesta(
                empresaId, nombreArchivo, hash, granularidad, periodoDesde, periodoHasta);
        ArchivoIngesta saved = repository.save(archivo);

        auditoriaIntegridadRepository.save(
                new AuditoriaIntegridad(saved.getId(), hash, AuditoriaIntegridad.RESULTADO_VALIDO));

        fileStorage.save(saved.getId(), contenido);
        processor.procesar(saved.getId(), empresaId, granularidad, contenido);

        return IngestionMapper.toUploadResponseDto(saved, "Archivo recibido, procesando");
    }

    public Page<IngestionHistoryItemDto> getHistory(String empresaId, Pageable pageable) {
        return repository.findByEmpresaIdOrderByFechaRecepcionDesc(empresaId, pageable)
                .map(archivo -> IngestionMapper.toDto(
                        archivo,
                        ventaMensualCountRepository.countByArchivoId(archivo.getId())
                                + ventaSemanalCountRepository.countByArchivoId(archivo.getId())));
    }

    public UploadResponseDto resync(String empresaId, String archivoId) {
        ArchivoIngesta archivo = repository.findByIdAndEmpresaId(archivoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo de ingesta no encontrado: " + archivoId));
        byte[] contenido = fileStorage.read(archivo.getId());
        archivo.setEstado(ArchivoIngesta.ESTADO_PENDIENTE);
        repository.save(archivo);
        processor.procesar(archivo.getId(), empresaId, archivo.getGranularidad(), contenido);
        return IngestionMapper.toUploadResponseDto(archivo, "Reintentando procesamiento");
    }

    public void borrar(String empresaId, String archivoId) {
        ArchivoIngesta archivo = repository.findByIdAndEmpresaId(archivoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo de ingesta no encontrado: " + archivoId));
        archivo.setEstado(ArchivoIngesta.ESTADO_ELIMINADO);
        repository.save(archivo);
    }

    private static final List<String> EXTENSIONES_PERMITIDAS = List.of(".csv", ".xlsx");

    private static final List<String> CONTENT_TYPES_PERMITIDOS = List.of(
            "text/csv", "application/csv", "text/plain", "application/vnd.ms-excel", "application/octet-stream",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/zip");

    private String validarNombreArchivo(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidFileException("El archivo no tiene nombre");
        }
        String nombre = Path.of(originalFilename).getFileName().toString().trim();
        String nombreLower = nombre.toLowerCase(Locale.ROOT);
        boolean extensionValida = EXTENSIONES_PERMITIDAS.stream().anyMatch(nombreLower::endsWith);
        if (!extensionValida) {
            throw new InvalidFileException("Solo se aceptan archivos .csv o .xlsx (recibido: " + nombre + ")");
        }
        return nombre.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void validarContentType(String contentType) {
        if (contentType != null && !CONTENT_TYPES_PERMITIDOS.contains(contentType)) {
            throw new InvalidFileException("Tipo de archivo no permitido: " + contentType);
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception e) {
            throw new InvalidFileException("No se pudo leer el archivo recibido", e);
        }
    }

    private String sha256(byte[] contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenido);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new InvalidFileException("No se pudo calcular el hash del archivo", e);
        }
    }
}
