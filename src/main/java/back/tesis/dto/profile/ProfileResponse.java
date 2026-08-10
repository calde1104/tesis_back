package back.tesis.dto.profile;

import back.tesis.dto.auth.EmpresaResumenDto;

public record ProfileResponse(
        String usuarioId,
        String email,
        String rol,
        EmpresaResumenDto empresa,
        String foto
) {
}
