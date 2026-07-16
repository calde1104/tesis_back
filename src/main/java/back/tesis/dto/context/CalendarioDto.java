package back.tesis.dto.context;

public record CalendarioDto(
        int anio,
        int mes,
        int cantidadFeriados,
        boolean tieneFestivoComercial,
        String nombreFestivo,
        boolean quincenaAguinaldo,
        boolean inicioCicloEscolar,
        boolean cicloCobroMensual
) {
}
