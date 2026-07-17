package back.tesis.modules.models;

import back.tesis.dto.models.ModelVersionDto;
import back.tesis.mapper.models.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelService {

    private final ModeloVersionRepository repository;

    public ModelService(ModeloVersionRepository repository) {
        this.repository = repository;
    }

    public List<ModelVersionDto> listModels(String empresaId) {
        return repository.findByEmpresaIdOrderByFechaEntrenamientoDesc(empresaId).stream()
                .map(ModelMapper::toDto)
                .toList();
    }
}
