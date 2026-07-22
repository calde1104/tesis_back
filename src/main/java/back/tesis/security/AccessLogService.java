package back.tesis.security;

import back.tesis.entities.LogAcceso;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AccessLogService {

    private final LogAccesoRepository repository;

    public AccessLogService(LogAccesoRepository repository) {
        this.repository = repository;
    }

    @Async
    public void registrar(String usuarioId, String empresaId, String accion, String endpoint, String ipOrigen, String resultado) {
        repository.save(new LogAcceso(usuarioId, empresaId, accion, endpoint, ipOrigen, resultado));
    }
}
