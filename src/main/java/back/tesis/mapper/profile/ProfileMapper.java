package back.tesis.mapper.profile;

import back.tesis.dto.auth.EmpresaResumenDto;
import back.tesis.dto.profile.ProfileResponse;
import back.tesis.entities.Usuario;

import java.util.Base64;

public final class ProfileMapper {

    private ProfileMapper() {
    }

    public static ProfileResponse toDto(Usuario usuario, String rolNombre, EmpresaResumenDto empresa) {
        return new ProfileResponse(
                usuario.getId(),
                usuario.getEmail(),
                rolNombre,
                empresa,
                fotoDataUri(usuario)
        );
    }

    private static String fotoDataUri(Usuario usuario) {
        byte[] bytes = usuario.getFotoPerfil();
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "data:" + usuario.getFotoPerfilTipo() + ";base64," + base64;
    }
}
