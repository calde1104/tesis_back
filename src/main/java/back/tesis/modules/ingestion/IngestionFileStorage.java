package back.tesis.modules.ingestion;

import back.tesis.common.exceptions.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class IngestionFileStorage {

    private final Path baseDir;

    public IngestionFileStorage(@Value("${app.ingestion.storage-dir:./data/ingestion-uploads}") String storageDir) {
        this.baseDir = Path.of(storageDir);
    }

    public void save(String archivoId, byte[] contenido) {
        try {
            Files.createDirectories(baseDir);
            Files.write(pathFor(archivoId), contenido);
        } catch (IOException e) {
            throw new InvalidFileException("No se pudo guardar el archivo de ingesta en disco", e);
        }
    }

    public byte[] read(String archivoId) {
        try {
            return Files.readAllBytes(pathFor(archivoId));
        } catch (IOException e) {
            throw new InvalidFileException("No se encontro el contenido original del archivo (id=" + archivoId + ")", e);
        }
    }

    private Path pathFor(String archivoId) {
        return baseDir.resolve(archivoId + ".csv");
    }
}
