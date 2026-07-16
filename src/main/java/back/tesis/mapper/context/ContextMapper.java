package back.tesis.mapper.context;

import back.tesis.dto.context.CalendarioDto;
import back.tesis.dto.context.ClimaDto;
import back.tesis.dto.context.MacroDto;
import back.tesis.entities.VariableCalendario;
import back.tesis.entities.VariableClimatica;
import back.tesis.entities.VariableMacroeconomica;

public final class ContextMapper {

    private ContextMapper() {
    }

    public static MacroDto toDto(VariableMacroeconomica entity) {
        return new MacroDto(
                entity.getAnio(),
                entity.getMes(),
                entity.getIpcIndice(),
                entity.getIpcVarInteranual(),
                entity.getTipoCambioPygUsd(),
                entity.getPbiCrecimiento(),
                entity.getTasaDesempleo(),
                entity.getRemesasUsd(),
                entity.getInversionExtUsd()
        );
    }

    public static ClimaDto toDto(VariableClimatica entity) {
        return new ClimaDto(
                entity.getAnio(),
                entity.getMes(),
                entity.getTempMaxPromedio(),
                entity.getTempMediaPromedio(),
                entity.getPrecipitacionMm()
        );
    }

    public static CalendarioDto toDto(VariableCalendario entity) {
        return new CalendarioDto(
                entity.getAnio(),
                entity.getMes(),
                entity.getCantidadFeriados(),
                entity.isTieneFestivoComercial(),
                entity.getNombreFestivo(),
                entity.isQuincenaAguinaldo(),
                entity.isInicioCicloEscolar(),
                entity.isCicloCobroMensual()
        );
    }
}
