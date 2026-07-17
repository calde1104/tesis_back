package back.tesis.mapper.config;

import back.tesis.dto.config.ConfigResponseDto;
import back.tesis.entities.ConfigSistema;

public final class ConfigMapper {

    private ConfigMapper() {
    }

    public static ConfigResponseDto toDto(ConfigSistema entity) {
        return new ConfigResponseDto(
                entity.getId(), entity.getEmpresaId(), entity.getProductoId(),
                entity.getParametro(), entity.getValor());
    }
}
