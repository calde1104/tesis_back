package back.tesis.modules.auth;

import back.tesis.entities.Usuario;
import back.tesis.entities.UsuarioAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    List<UsuarioAuth> findByEmail(String email);
}
