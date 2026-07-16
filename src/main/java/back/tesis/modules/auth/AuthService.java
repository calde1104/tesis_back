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

@Service
public class AuthService {

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
        Usuario usuario = usuarioRepository.findByEmpresaIdAndEmail(request.empresaId(), request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales invalidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales invalidas");
        }

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
