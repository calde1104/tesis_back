package back.tesis.mapper.profile;

import back.tesis.dto.auth.EmpresaResumenDto;
import back.tesis.dto.profile.ProfileResponse;
import back.tesis.entities.Usuario;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileMapperTest {

    private final EmpresaResumenDto empresa = new EmpresaResumenDto("emp-1", "ACME", "30-1");

    @Test
    void sinFotoDevuelveNull() {
        Usuario usuario = new Usuario("emp-1", 1, "user@tesis.local", "hash");

        ProfileResponse dto = ProfileMapper.toDto(usuario, "ADMIN", empresa);

        assertThat(dto.email()).isEqualTo("user@tesis.local");
        assertThat(dto.rol()).isEqualTo("ADMIN");
        assertThat(dto.empresa()).isEqualTo(empresa);
        assertThat(dto.foto()).isNull();
    }

    @Test
    void conFotoDevuelveDataUriBase64() {
        Usuario usuario = new Usuario("emp-1", 1, "user@tesis.local", "hash");
        usuario.actualizarFoto(new byte[]{1, 2, 3}, "image/png");

        ProfileResponse dto = ProfileMapper.toDto(usuario, "ADMIN", empresa);

        assertThat(dto.foto()).isEqualTo("data:image/png;base64,AQID");
    }
}
