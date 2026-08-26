package back.tesis.modules.coldstart;

import back.tesis.dto.coldstart.ColdStartRequest;
import back.tesis.dto.coldstart.ColdStartResponse;

public interface ColdStartClient {
    ColdStartResponse predict(ColdStartRequest request);
}
