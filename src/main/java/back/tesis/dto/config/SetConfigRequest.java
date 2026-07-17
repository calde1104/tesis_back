package back.tesis.dto.config;

import jakarta.validation.constraints.NotBlank;

public record SetConfigRequest(
        @NotBlank(message = "parametro es obligatorio") String parametro,
        @NotBlank(message = "valor es obligatorio") String valor,
        String productoId
) {
}
