package back.tesis.dto.ingestion;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record IngestionHistoryItemDto(
        String archivoId,
        String nombreArchivo,
        String estado,
        String granularidad,
        LocalDate periodoDesde,
        LocalDate periodoHasta,
        LocalDateTime fechaRecepcion,
        long cantRegistros
) {
}
