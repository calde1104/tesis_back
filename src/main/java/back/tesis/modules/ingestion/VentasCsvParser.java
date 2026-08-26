package back.tesis.modules.ingestion;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class VentasCsvParser {

    private VentasCsvParser() {
    }

    static ParseResult parse(byte[] contenido) {
        List<VentaRow> filas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(contenido), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                errores.add("El archivo esta vacio");
                return new ParseResult(filas, errores);
            }

            String linea;
            int lineNumber = 1;
            while ((linea = reader.readLine()) != null) {
                lineNumber++;
                if (linea.isBlank()) {
                    continue;
                }
                try {
                    filas.add(VentaFilaParser.parseColumnas(linea.split(",", -1), lineNumber));
                } catch (VentaFilaParser.LineaInvalidaException e) {
                    errores.add(e.getMessage());
                }
            }
        } catch (IOException e) {
            errores.add("No se pudo leer el archivo: " + e.getMessage());
        }

        return new ParseResult(filas, errores);
    }
}
