package back.tesis.modules.ingestion;

import java.util.List;

record ParseResult(List<VentaRow> filas, List<String> errores) {
}
