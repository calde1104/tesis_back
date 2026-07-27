package back.tesis.modules.system;

import back.tesis.dto.system.SystemInfoDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/{empresaId}/system-info")
public class SystemInfoController {

    private final SystemInfoService systemInfoService;

    public SystemInfoController(SystemInfoService systemInfoService) {
        this.systemInfoService = systemInfoService;
    }

    @GetMapping
    public SystemInfoDto getSystemInfo(@PathVariable String empresaId) {
        return systemInfoService.getSystemInfo(empresaId);
    }
}
