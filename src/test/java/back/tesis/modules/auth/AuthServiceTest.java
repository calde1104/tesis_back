package back.tesis.modules.auth;

import back.tesis.common.exceptions.InvalidCredentialsException;
import back.tesis.dto.auth.LoginRequest;
import back.tesis.entities.Empresa;
import back.tesis.entities.EmpresaRepository;
import back.tesis.entities.Rol;
import back.tesis.entities.Usuario;
import back.tesis.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @Test
    void loginSucceedsWithValidCredentials() throws Exception {
        Usuario usuario = newUsuario("emp-1", 1, "user@test.com", "hashed");
        Rol rol = newRol(1, "ADMIN");
        Empresa empresa = newEmpresa("emp-1", "Acme", "800123456");

        when(usuarioRepository.findByEmpresaIdAndEmail("emp-1", "user@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("plain", "hashed")).thenReturn(true);
        when(rolRepository.findById(1)).thenReturn(Optional.of(rol));
        when(empresaRepository.findById("emp-1")).thenReturn(Optional.of(empresa));
        when(jwtService.generateToken(usuario.getId(), "emp-1", "ADMIN")).thenReturn("jwt-token");

        AuthService service = new AuthService(usuarioRepository, rolRepository, empresaRepository, passwordEncoder, jwtService);

        var response = service.login(new LoginRequest("emp-1", "user@test.com", "plain"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.rol()).isEqualTo("ADMIN");
        assertThat(response.empresa().nombre()).isEqualTo("Acme");
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        Usuario usuario = newUsuario("emp-1", 1, "user@test.com", "hashed");
        when(usuarioRepository.findByEmpresaIdAndEmail("emp-1", "user@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        AuthService service = new AuthService(usuarioRepository, rolRepository, empresaRepository, passwordEncoder, jwtService);

        assertThatThrownBy(() -> service.login(new LoginRequest("emp-1", "user@test.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginFailsWhenUserNotFoundForEmpresa() {
        when(usuarioRepository.findByEmpresaIdAndEmail("emp-1", "nadie@test.com")).thenReturn(Optional.empty());

        AuthService service = new AuthService(usuarioRepository, rolRepository, empresaRepository, passwordEncoder, jwtService);

        assertThatThrownBy(() -> service.login(new LoginRequest("emp-1", "nadie@test.com", "x")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private Usuario newUsuario(String empresaId, int rolId, String email, String hash) throws Exception {
        Usuario usuario = new Usuario(empresaId, rolId, email, hash);
        var idField = usuario.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(usuario, "usr-1");
        return usuario;
    }

    private Rol newRol(int id, String nombre) throws Exception {
        var constructor = Rol.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Rol rol = constructor.newInstance();
        set(rol, "id", id);
        set(rol, "nombre", nombre);
        return rol;
    }

    private Empresa newEmpresa(String id, String nombre, String cuit) throws Exception {
        var constructor = Empresa.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Empresa empresa = constructor.newInstance();
        var idField = empresa.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(empresa, id);
        set(empresa, "nombre", nombre);
        set(empresa, "cuit", cuit);
        return empresa;
    }

    private void set(Object target, String field, Object value) throws Exception {
        var f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}
