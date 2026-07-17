package back.tesis.dto.ingestion;

public record UploadResponseDto(
        String archivoId,
        String estado,
        String mensaje
) {
}
