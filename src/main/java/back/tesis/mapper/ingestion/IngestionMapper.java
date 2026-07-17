package back.tesis.mapper.ingestion;

import back.tesis.dto.ingestion.IngestionHistoryItemDto;
import back.tesis.dto.ingestion.UploadResponseDto;
import back.tesis.entities.ArchivoIngesta;

public final class IngestionMapper {

    private IngestionMapper() {
    }

    public static IngestionHistoryItemDto toDto(ArchivoIngesta entity) {
        return new IngestionHistoryItemDto(
                entity.getId(), entity.getNombreArchivo(), entity.getEstado(), entity.getGranularidad(),
                entity.getPeriodoDesde(), entity.getPeriodoHasta(), entity.getFechaRecepcion());
    }

    public static UploadResponseDto toUploadResponseDto(ArchivoIngesta entity, String mensaje) {
        return new UploadResponseDto(entity.getId(), entity.getEstado(), mensaje);
    }
}
