package back.tesis.modules.config;

import back.tesis.dto.config.ConfigResponseDto;
import back.tesis.dto.config.SetConfigRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/{empresaId}/config")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public List<ConfigResponseDto> getConfig(@PathVariable String empresaId) {
        return configService.getConfig(empresaId);
    }

    @PostMapping
    public ConfigResponseDto setConfig(@PathVariable String empresaId, @Valid @RequestBody SetConfigRequest request) {
        return configService.setParametro(empresaId, request);
    }
}
