package back.tesis.modules.coldstart;

import back.tesis.dto.coldstart.ColdStartRequest;
import back.tesis.dto.coldstart.ColdStartResponse;
import org.springframework.stereotype.Service;

@Service
public class ColdStartService {

    private final ColdStartClient coldStartClient;

    public ColdStartService(ColdStartClient coldStartClient) {
        this.coldStartClient = coldStartClient;
    }

    public ColdStartResponse predict(String empresaId, ColdStartRequest request) {
        return coldStartClient.predict(request);
    }
}
