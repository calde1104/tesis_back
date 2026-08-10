package back.tesis.modules.profile;

import back.tesis.common.exceptions.InvalidFileException;
import back.tesis.common.exceptions.ResourceNotFoundException;
import back.tesis.dto.profile.ProfileResponse;
import back.tesis.entities.Empresa;
import back.tesis.entities.EmpresaRepository;
import back.tesis.entities.Rol;
import back.tesis.entities.Usuario;
import back.tesis.modules.auth.RolRepository;
import back.tesis.modules.auth.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private EmpresaRepository empresaRepository;
    @InjectMocks private ProfileService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("emp-1", 1, "user@tesis.local", "hash");
    }

    private void stubLookups() throws Exception {
        when(usuarioRepository.findById("user-1")).thenReturn(Optional.of(usuario));
        when(rolRepository.findById(1)).thenReturn(Optional.of(newRol(1, "ADMIN")));
        when(empresaRepository.findById("emp-1")).thenReturn(Optional.of(newEmpresa("emp-1", "ACME", "30-1")));
    }

    @Test
    void getProfileDevuelvePerfilSinFoto() throws Exception {
        stubLookups();

        ProfileResponse dto = service.getProfile("user-1");

        assertThat(dto.email()).isEqualTo("user@tesis.local");
        assertThat(dto.rol()).isEqualTo("ADMIN");
        assertThat(dto.foto()).isNull();
    }

    @Test
    void getProfileUsuarioInexistenteLanza404() {
        when(usuarioRepository.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile("nope"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateFotoPersisteBytesYTipo() throws Exception {
        stubLookups();
        MultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", new byte[]{1, 2, 3});

        ProfileResponse dto = service.updateFoto("user-1", file);

        assertThat(usuario.getFotoPerfil()).containsExactly(1, 2, 3);
        assertThat(usuario.getFotoPerfilTipo()).isEqualTo("image/png");
        assertThat(dto.foto()).isEqualTo("data:image/png;base64,AQID");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void updateFotoRechazaTipoInvalido() {
        lenient().when(usuarioRepository.findById("user-1")).thenReturn(Optional.of(usuario));
        MultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[]{1});

        assertThatThrownBy(() -> service.updateFoto("user-1", file))
                .isInstanceOf(InvalidFileException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void updateFotoRechazaArchivoVacio() {
        lenient().when(usuarioRepository.findById("user-1")).thenReturn(Optional.of(usuario));
        MultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", new byte[]{});

        assertThatThrownBy(() -> service.updateFoto("user-1", file))
                .isInstanceOf(InvalidFileException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void updateFotoRechazaArchivoMayorA2MB() {
        lenient().when(usuarioRepository.findById("user-1")).thenReturn(Optional.of(usuario));
        byte[] grande = new byte[2 * 1024 * 1024 + 1];
        MultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", grande);

        assertThatThrownBy(() -> service.updateFoto("user-1", file))
                .isInstanceOf(InvalidFileException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deleteFotoLimpiaLaFoto() {
        usuario.actualizarFoto(new byte[]{7}, "image/png");
        when(usuarioRepository.findById("user-1")).thenReturn(Optional.of(usuario));

        service.deleteFoto("user-1");

        assertThat(usuario.getFotoPerfil()).isNull();
        assertThat(usuario.getFotoPerfilTipo()).isNull();
        verify(usuarioRepository).save(usuario);
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
