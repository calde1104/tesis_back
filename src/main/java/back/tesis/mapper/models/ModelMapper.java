package back.tesis.mapper.models;

import back.tesis.dto.models.ModelVersionDto;
import back.tesis.entities.ModeloVersion;

public final class ModelMapper {

    private ModelMapper() {
    }

    public static ModelVersionDto toDto(ModeloVersion entity) {
        return new ModelVersionDto(
                entity.getId(), entity.getVersion(), entity.getMae(),
                entity.getRmse(), entity.getMape(), entity.isActivo());
    }
}
