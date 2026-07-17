package back.tesis.modules.ingestion;

import back.tesis.common.PaginatedResponse;
import back.tesis.dto.ingestion.IngestionHistoryItemDto;
import back.tesis.dto.ingestion.UploadResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/{empresaId}/ingestion")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/upload")
    public UploadResponseDto upload(
            @PathVariable String empresaId,
            @RequestParam MultipartFile file,
            @RequestParam String granularidad,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodoDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodoHasta
    ) {
        return ingestionService.upload(empresaId, file, granularidad, periodoDesde, periodoHasta);
    }

    @GetMapping("/history")
    public PaginatedResponse<IngestionHistoryItemDto> getHistory(@PathVariable String empresaId, Pageable pageable) {
        return PaginatedResponse.of(ingestionService.getHistory(empresaId, pageable));
    }

    @PostMapping("/{archivoId}/resync")
    public UploadResponseDto resync(@PathVariable String empresaId, @PathVariable String archivoId) {
        return ingestionService.resync(empresaId, archivoId);
    }

    @DeleteMapping("/{archivoId}/borrado")
    public ResponseEntity<Void> borrar(@PathVariable String empresaId, @PathVariable String archivoId) {
        ingestionService.borrar(empresaId, archivoId);
        return ResponseEntity.noContent().build();
    }
}
