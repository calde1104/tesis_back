package back.tesis.modules.config;

import back.tesis.dto.config.ConfigResponseDto;
import back.tesis.dto.config.SetConfigRequest;
import back.tesis.entities.ConfigSistema;
import back.tesis.mapper.config.ConfigMapper;
import back.tesis.modules.notificaciones.NotificacionesService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigService {

    private final ConfigSistemaRepository repository;
    private final NotificacionesService notificacionesService;

    public ConfigService(ConfigSistemaRepository repository, NotificacionesService notificacionesService) {
        this.repository = repository;
        this.notificacionesService = notificacionesService;
    }

    public List<ConfigResponseDto> getConfig(String empresaId) {
        return repository.findByEmpresaIdOrderByParametroAsc(empresaId).stream()
                .map(ConfigMapper::toDto)
                .toList();
    }

    public ConfigResponseDto setParametro(String empresaId, SetConfigRequest request) {
        ConfigSistema entity = repository
                .findByEmpresaIdAndProductoIdAndParametro(empresaId, request.productoId(), request.parametro())
                .orElseGet(() -> new ConfigSistema(empresaId, request.productoId(), request.parametro(), request.valor()));

        entity.setValor(request.valor());
        ConfigSistema saved = repository.save(entity);

        if (NotificacionesService.PARAMETRO_UMBRAL_DEMANDA.equals(saved.getParametro()) && saved.getProductoId() != null) {
            notificacionesService.chequearUmbralInmediato(saved);
        }

        return ConfigMapper.toDto(saved);
    }
}
