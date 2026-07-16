package back.tesis.modules.context;

import back.tesis.dto.context.CalendarioDto;
import back.tesis.dto.context.ClimaDto;
import back.tesis.dto.context.MacroDto;
import back.tesis.mapper.context.ContextMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContextService {

    private final VariableMacroeconomicaRepository macroRepository;
    private final VariableClimaticaRepository climaRepository;
    private final VariableCalendarioRepository calendarioRepository;

    public ContextService(
            VariableMacroeconomicaRepository macroRepository,
            VariableClimaticaRepository climaRepository,
            VariableCalendarioRepository calendarioRepository
    ) {
        this.macroRepository = macroRepository;
        this.climaRepository = climaRepository;
        this.calendarioRepository = calendarioRepository;
    }

    public List<MacroDto> getMacro() {
        return macroRepository.findAllByOrderByAnioAscMesAsc().stream()
                .map(ContextMapper::toDto)
                .toList();
    }

    public List<ClimaDto> getClima() {
        return climaRepository.findAllByOrderByAnioAscMesAsc().stream()
                .map(ContextMapper::toDto)
                .toList();
    }

    public List<CalendarioDto> getCalendario() {
        return calendarioRepository.findAllByOrderByAnioAscMesAsc().stream()
                .map(ContextMapper::toDto)
                .toList();
    }
}
