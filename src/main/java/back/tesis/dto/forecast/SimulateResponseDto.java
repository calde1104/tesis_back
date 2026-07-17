package back.tesis.dto.forecast;

import java.util.Map;

public record SimulateResponseDto(boolean simulado, Map<String, Object> resultado) {
    public SimulateResponseDto {
    }
}
