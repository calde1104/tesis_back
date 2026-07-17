package back.tesis.modules.ingestion;

import back.tesis.common.exceptions.ConflictException;
import back.tesis.common.exceptions.InvalidFileException;
import back.tesis.common.exceptions.ResourceNotFoundException;
import back.tesis.dto.ingestion.IngestionHistoryItemDto;
import back.tesis.dto.ingestion.UploadResponseDto;
import back.tesis.entities.ArchivoIngesta;
import back.tesis.mapper.ingestion.IngestionMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;

@Service
public class IngestionService {

    private final ArchivoIngestaRepository repository;

    public IngestionService(ArchivoIngestaRepository repository) {
        this.repository = repository;
    }

    public UploadResponseDto upload(String empresaId, MultipartFile file, String granularidad,
                                     LocalDate periodoDesde, LocalDate periodoHasta) {
        String hash = sha256(file);
        if (repository.existsByHashSha256(hash)) {
            throw new ConflictException("El archivo ya fue cargado previamente (hash duplicado)");
        }

        ArchivoIngesta archivo = new ArchivoIngesta(
                empresaId, file.getOriginalFilename(), hash, granularidad, periodoDesde, periodoHasta);
        ArchivoIngesta saved = repository.save(archivo);

        procesarAsync(saved.getId());

        return IngestionMapper.toUploadResponseDto(saved, "Archivo recibido, pendiente de procesamiento");
    }

    private void procesarAsync(String archivoId) {
    }

    public Page<IngestionHistoryItemDto> getHistory(String empresaId, Pageable pageable) {
        return repository.findByEmpresaIdOrderByFechaRecepcionDesc(empresaId, pageable)
                .map(IngestionMapper::toDto);
    }

    public UploadResponseDto resync(String empresaId, String archivoId) {
        ArchivoIngesta archivo = repository.findByIdAndEmpresaId(archivoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo de ingesta no encontrado: " + archivoId));
        archivo.setEstado(ArchivoIngesta.ESTADO_PENDIENTE);
        repository.save(archivo);
        procesarAsync(archivo.getId());
        return IngestionMapper.toUploadResponseDto(archivo, "Reintentando procesamiento");
    }

    public void borrar(String empresaId, String archivoId) {
        ArchivoIngesta archivo = repository.findByIdAndEmpresaId(archivoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo de ingesta no encontrado: " + archivoId));
        archivo.setEstado(ArchivoIngesta.ESTADO_ELIMINADO);
        repository.save(archivo);
    }

    private String sha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new InvalidFileException("No se pudo calcular el hash del archivo", e);
        }
    }
}
