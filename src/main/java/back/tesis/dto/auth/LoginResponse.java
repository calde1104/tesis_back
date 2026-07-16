package back.tesis.dto.auth;

public record LoginResponse(
        String token,
        String usuarioId,
        String rol,
        EmpresaResumenDto empresa
) {
}
