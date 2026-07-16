package back.tesis.dto.context;

import java.math.BigDecimal;

public record MacroDto(
        int anio,
        int mes,
        Float ipcIndice,
        Float ipcVarInteranual,
        Float tipoCambioPygUsd,
        Float pbiCrecimiento,
        Float tasaDesempleo,
        BigDecimal remesasUsd,
        BigDecimal inversionExtUsd
) {
}
