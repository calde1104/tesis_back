package back.tesis.modules.coldstart;

import back.tesis.dto.coldstart.ColdStartRequest;
import back.tesis.dto.coldstart.ColdStartResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/{empresaId}/cold-start")
public class ColdStartController {

    private final ColdStartService coldStartService;

    public ColdStartController(ColdStartService coldStartService) {
        this.coldStartService = coldStartService;
    }

    @PostMapping("/predict")
    public ColdStartResponse predict(@PathVariable String empresaId, @Valid @RequestBody ColdStartRequest request) {
        return coldStartService.predict(empresaId, request);
    }
}
