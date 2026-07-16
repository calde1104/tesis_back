package back.tesis.modules.context;

import back.tesis.entities.VariableMacroeconomica;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContextServiceTest {

    @Mock
    private VariableMacroeconomicaRepository macroRepository;
    @Mock
    private VariableClimaticaRepository climaRepository;
    @Mock
    private VariableCalendarioRepository calendarioRepository;

    @Test
    void getMacroReturnsAllOrderedByPeriodo() throws Exception {
        VariableMacroeconomica entity = mockMacro();
        when(macroRepository.findAllByOrderByAnioAscMesAsc()).thenReturn(List.of(entity));

        ContextService service = new ContextService(macroRepository, climaRepository, calendarioRepository);

        var result = service.getMacro();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).anio()).isEqualTo(2024);
    }

    private VariableMacroeconomica mockMacro() throws Exception {
        var constructor = VariableMacroeconomica.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        VariableMacroeconomica entity = constructor.newInstance();
        var anioField = VariableMacroeconomica.class.getDeclaredField("anio");
        anioField.setAccessible(true);
        anioField.set(entity, 2024);
        return entity;
    }
}
