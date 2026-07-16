package back.tesis.modules.context;

import back.tesis.dto.context.CalendarioDto;
import back.tesis.dto.context.ClimaDto;
import back.tesis.dto.context.MacroDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/context")
public class ContextController {

    private final ContextService contextService;

    public ContextController(ContextService contextService) {
        this.contextService = contextService;
    }

    @GetMapping("/macro")
    public List<MacroDto> getMacro() {
        return contextService.getMacro();
    }

    @GetMapping("/clima")
    public List<ClimaDto> getClima() {
        return contextService.getClima();
    }

    @GetMapping("/calendario")
    public List<CalendarioDto> getCalendario() {
        return contextService.getCalendario();
    }
}
