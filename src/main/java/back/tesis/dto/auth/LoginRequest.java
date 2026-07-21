package back.tesis.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email(message = "email invalido") String email,
        @NotBlank(message = "password es obligatorio") String password
) {
}
