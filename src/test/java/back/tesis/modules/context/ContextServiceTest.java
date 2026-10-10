package back.tesis.modules.context;

import back.tesis.entities.VariableCalendario;
import back.tesis.entities.VariableClimatica;
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

    @Test
    void getClimaReturnsAllOrderedByPeriodo() throws Exception {
        VariableClimatica entity = instanciarConAnio(VariableClimatica.class, 2024);
        when(climaRepository.findAllByOrderByAnioAscMesAsc()).thenReturn(List.of(entity));

        ContextService service = new ContextService(macroRepository, climaRepository, calendarioRepository);

        var result = service.getClima();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).anio()).isEqualTo(2024);
    }

    @Test
    void getCalendarioReturnsAllOrderedByPeriodo() throws Exception {
        VariableCalendario entity = instanciarConAnio(VariableCalendario.class, 2024);
        when(calendarioRepository.findAllByOrderByAnioAscMesAsc()).thenReturn(List.of(entity));

        ContextService service = new ContextService(macroRepository, climaRepository, calendarioRepository);

        var result = service.getCalendario();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).anio()).isEqualTo(2024);
    }

    private VariableMacroeconomica mockMacro() throws Exception {
        return instanciarConAnio(VariableMacroeconomica.class, 2024);
    }

    private static <T> T instanciarConAnio(Class<T> clase, int anio) throws Exception {
        var constructor = clase.getDeclaredConstructor();
        constructor.setAccessible(true);
        T entity = constructor.newInstance();
        var anioField = clase.getDeclaredField("anio");
        anioField.setAccessible(true);
        anioField.set(entity, anio);
        return entity;
    }
}
