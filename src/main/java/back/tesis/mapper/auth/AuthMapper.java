package back.tesis.mapper.auth;

import back.tesis.dto.auth.EmpresaResumenDto;
import back.tesis.dto.auth.LoginResponse;
import back.tesis.entities.Empresa;

public final class AuthMapper {

    private AuthMapper() {
    }

    public static EmpresaResumenDto toDto(Empresa entity) {
        return new EmpresaResumenDto(entity.getId(), entity.getNombre(), entity.getCuit());
    }

    public static LoginResponse toDto(String usuarioId, String rolNombre, EmpresaResumenDto empresa, String token) {
        return new LoginResponse(token, usuarioId, rolNombre, empresa);
    }
}
