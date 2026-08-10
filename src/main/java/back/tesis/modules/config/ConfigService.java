package back.tesis.modules.config;

import back.tesis.dto.config.ConfigResponseDto;
import back.tesis.dto.config.SetConfigRequest;
import back.tesis.entities.ConfigSistema;
import back.tesis.mapper.config.ConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigService {

    private final ConfigSistemaRepository repository;

    public ConfigService(ConfigSistemaRepository repository) {
        this.repository = repository;
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

        return ConfigMapper.toDto(saved);
    }
}
