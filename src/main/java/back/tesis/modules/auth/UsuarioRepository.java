package back.tesis.modules.auth;

import back.tesis.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmpresaIdAndEmail(String empresaId, String email);

    List<Usuario> findByEmail(String email);
}
