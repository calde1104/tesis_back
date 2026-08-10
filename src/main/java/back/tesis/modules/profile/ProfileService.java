package back.tesis.modules.profile;

import back.tesis.common.exceptions.InvalidFileException;
import back.tesis.common.exceptions.ResourceNotFoundException;
import back.tesis.dto.auth.EmpresaResumenDto;
import back.tesis.dto.profile.ProfileResponse;
import back.tesis.entities.Empresa;
import back.tesis.entities.EmpresaRepository;
import back.tesis.entities.Rol;
import back.tesis.entities.Usuario;
import back.tesis.mapper.auth.AuthMapper;
import back.tesis.mapper.profile.ProfileMapper;
import back.tesis.modules.auth.RolRepository;
import back.tesis.modules.auth.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
public class ProfileService {

    private static final long MAX_BYTES = 2L * 1024 * 1024;
    private static final Set<String> TIPOS_PERMITIDOS =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EmpresaRepository empresaRepository;

    public ProfileService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            EmpresaRepository empresaRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.empresaRepository = empresaRepository;
    }

    public ProfileResponse getProfile(String usuarioId) {
        return armarProfile(cargarUsuario(usuarioId));
    }

    public ProfileResponse updateFoto(String usuarioId, MultipartFile file) {
        validar(file);
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new InvalidFileException("No se pudo leer la imagen", e);
        }
        Usuario usuario = cargarUsuario(usuarioId);
        usuario.actualizarFoto(bytes, file.getContentType());
        usuarioRepository.save(usuario);
        return armarProfile(usuario);
    }

    public void deleteFoto(String usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        if (usuario.getFotoPerfil() != null) {
            usuario.quitarFoto();
            usuarioRepository.save(usuario);
        }
    }

    private void validar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("El archivo de imagen esta vacio");
        }
        String tipo = file.getContentType();
        if (tipo == null || !TIPOS_PERMITIDOS.contains(tipo)) {
            throw new InvalidFileException(
                    "Tipo de imagen no permitido: " + tipo + ". Se aceptan JPEG, PNG o WebP");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new InvalidFileException("La imagen supera el limite de 2 MB");
        }
    }

    private Usuario cargarUsuario(String usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado (id=" + usuarioId + ")"));
    }

    private ProfileResponse armarProfile(Usuario usuario) {
        Rol rol = rolRepository.findById(usuario.getRolId())
                .orElseThrow(() -> new ResourceNotFoundException("Rol de usuario invalido"));
        Empresa empresa = empresaRepository.findById(usuario.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa asociada invalida"));
        EmpresaResumenDto empresaResumen = AuthMapper.toDto(empresa);
        return ProfileMapper.toDto(usuario, rol.getNombre(), empresaResumen);
    }
}
