package back.tesis.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioFotoTest {

    private Usuario nuevoUsuario() {
        return new Usuario("emp-1", 1, "user@tesis.local", "hash");
    }

    @Test
    void actualizarFotoGuardaBytesYTipo() {
        Usuario usuario = nuevoUsuario();

        usuario.actualizarFoto(new byte[]{1, 2, 3}, "image/png");

        assertThat(usuario.getFotoPerfil()).containsExactly(1, 2, 3);
        assertThat(usuario.getFotoPerfilTipo()).isEqualTo("image/png");
    }

    @Test
    void quitarFotoDejaAmbosCamposEnNull() {
        Usuario usuario = nuevoUsuario();
        usuario.actualizarFoto(new byte[]{9}, "image/jpeg");

        usuario.quitarFoto();

        assertThat(usuario.getFotoPerfil()).isNull();
        assertThat(usuario.getFotoPerfilTipo()).isNull();
    }
}
