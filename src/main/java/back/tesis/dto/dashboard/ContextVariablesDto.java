package back.tesis.dto.dashboard;

import back.tesis.dto.context.CalendarioDto;
import back.tesis.dto.context.MacroDto;

public record ContextVariablesDto(
        MacroDto macro,
        CalendarioDto calendario
) {
}
