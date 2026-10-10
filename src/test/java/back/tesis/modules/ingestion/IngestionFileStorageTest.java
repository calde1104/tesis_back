package back.tesis.modules.ingestion;

import back.tesis.common.exceptions.InvalidFileException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IngestionFileStorageTest {

    @Test
    void guardaYLeeElMismoContenido(@TempDir Path tempDir) {
        IngestionFileStorage storage = new IngestionFileStorage(tempDir.toString());
        byte[] contenido = "sku,anio,mes,unidades_vendidas,monto_facturado\nSKU-1,2024,3,10,1000.00\n"
                .getBytes(StandardCharsets.UTF_8);

        storage.save("arch-1", contenido);
        byte[] leido = storage.read("arch-1");

        assertThat(leido).isEqualTo(contenido);
    }

    @Test
    void creaElDirectorioDeAlmacenamientoSiNoExiste(@TempDir Path tempDir) {
        Path subdirInexistente = tempDir.resolve("no-existe-aun");
        IngestionFileStorage storage = new IngestionFileStorage(subdirInexistente.toString());

        storage.save("arch-1", "contenido".getBytes(StandardCharsets.UTF_8));

        assertThat(subdirInexistente).isDirectory();
    }

    @Test
    void leerUnArchivoInexistenteLanzaInvalidFileException(@TempDir Path tempDir) {
        IngestionFileStorage storage = new IngestionFileStorage(tempDir.toString());

        assertThatThrownBy(() -> storage.read("arch-ausente"))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("arch-ausente");
    }
}
