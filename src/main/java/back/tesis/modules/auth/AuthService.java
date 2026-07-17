package back.tesis.modules.auth;

import back.tesis.common.exceptions.InvalidCredentialsException;
import back.tesis.dto.auth.EmpresaResumenDto;
import back.tesis.dto.auth.LoginRequest;
import back.tesis.dto.auth.LoginResponse;
import back.tesis.entities.Empresa;
import back.tesis.entities.EmpresaRepository;
import back.tesis.entities.Rol;
import back.tesis.entities.Usuario;
import back.tesis.mapper.auth.AuthMapper;
import back.tesis.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private static final String DUMMY_HASH =
            "$2a$10$ZHr6kd8e.5Jya9pKNccDdu5v1N6LATjajPEiHFAv6C4jq0w80fiG6";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            EmpresaRepository empresaRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmpresaIdAndEmail(request.empresaId(), request.email());

        boolean credencialesValidas = usuarioOpt
                .map(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseGet(() -> {
                    passwordEncoder.matches(request.password(), DUMMY_HASH);
                    return false;
                });

        if (!credencialesValidas) {
            throw new InvalidCredentialsException("Credenciales invalidas");
        }

        Usuario usuario = usuarioOpt.get();

        Rol rol = rolRepository.findById(usuario.getRolId())
                .orElseThrow(() -> new InvalidCredentialsException("Rol de usuario invalido"));
        Empresa empresa = empresaRepository.findById(usuario.getEmpresaId())
                .orElseThrow(() -> new InvalidCredentialsException("Empresa asociada invalida"));

        String token = jwtService.generateToken(usuario.getId(), empresa.getId(), rol.getNombre());

        EmpresaResumenDto empresaResumen = AuthMapper.toDto(empresa);
        return AuthMapper.toDto(usuario, rol.getNombre(), empresaResumen, token);
    }

    public void logout() {
    }
}
