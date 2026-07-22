package back.tesis.security;

import back.tesis.entities.LogAcceso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogAccesoRepository extends JpaRepository<LogAcceso, String> {
}
