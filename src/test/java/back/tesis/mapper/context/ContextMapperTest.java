package back.tesis.mapper.context;

import back.tesis.entities.VariableCalendario;
import back.tesis.entities.VariableClimatica;
import back.tesis.entities.VariableMacroeconomica;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ContextMapperTest {

    @Test
    void mapeaTodosLosCamposDeVariableMacroeconomica() throws Exception {
        VariableMacroeconomica entity = instanciar(VariableMacroeconomica.class);
        setField(entity, "anio", 2025);
        setField(entity, "mes", 6);
        setField(entity, "ipcIndice", 101.5f);
        setField(entity, "ipcVarInteranual", 4.5f);
        setField(entity, "tipoCambioPygUsd", 7200.0f);
        setField(entity, "pbiCrecimiento", 3.8f);
        setField(entity, "tasaDesempleo", 5.2f);
        setField(entity, "remesasUsd", new BigDecimal("50000000.00"));
        setField(entity, "inversionExtUsd", new BigDecimal("25000000.00"));

        var dto = ContextMapper.toDto(entity);

        assertThat(dto.anio()).isEqualTo(2025);
        assertThat(dto.mes()).isEqualTo(6);
        assertThat(dto.ipcIndice()).isEqualTo(101.5f);
        assertThat(dto.ipcVarInteranual()).isEqualTo(4.5f);
        assertThat(dto.tipoCambioPygUsd()).isEqualTo(7200.0f);
        assertThat(dto.pbiCrecimiento()).isEqualTo(3.8f);
        assertThat(dto.tasaDesempleo()).isEqualTo(5.2f);
        assertThat(dto.remesasUsd()).isEqualByComparingTo("50000000.00");
        assertThat(dto.inversionExtUsd()).isEqualByComparingTo("25000000.00");
    }

    @Test
    void mapeaTodosLosCamposDeVariableClimatica() throws Exception {
        VariableClimatica entity = instanciar(VariableClimatica.class);
        setField(entity, "anio", 2025);
        setField(entity, "mes", 1);
        setField(entity, "tempMaxPromedio", 32.5f);
        setField(entity, "tempMediaPromedio", 26.0f);
        setField(entity, "precipitacionMm", 120.0f);

        var dto = ContextMapper.toDto(entity);

        assertThat(dto.anio()).isEqualTo(2025);
        assertThat(dto.mes()).isEqualTo(1);
        assertThat(dto.tempMaxPromedio()).isEqualTo(32.5f);
        assertThat(dto.tempMediaPromedio()).isEqualTo(26.0f);
        assertThat(dto.precipitacionMm()).isEqualTo(120.0f);
    }

    @Test
    void mapeaTodosLosCamposDeVariableCalendario() throws Exception {
        VariableCalendario entity = instanciar(VariableCalendario.class);
        setField(entity, "anio", 2025);
        setField(entity, "mes", 12);
        setField(entity, "cantidadFeriados", 2);
        setField(entity, "tieneFestivoComercial", true);
        setField(entity, "nombreFestivo", "Navidad");
        setField(entity, "quincenaAguinaldo", true);
        setField(entity, "inicioCicloEscolar", false);
        setField(entity, "cicloCobroMensual", true);

        var dto = ContextMapper.toDto(entity);

        assertThat(dto.anio()).isEqualTo(2025);
        assertThat(dto.mes()).isEqualTo(12);
        assertThat(dto.cantidadFeriados()).isEqualTo(2);
        assertThat(dto.tieneFestivoComercial()).isTrue();
        assertThat(dto.nombreFestivo()).isEqualTo("Navidad");
        assertThat(dto.quincenaAguinaldo()).isTrue();
        assertThat(dto.inicioCicloEscolar()).isFalse();
        assertThat(dto.cicloCobroMensual()).isTrue();
    }

    private static <T> T instanciar(Class<T> clase) throws Exception {
        var constructor = clase.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static void setField(Object objetivo, String nombreCampo, Object valor) throws Exception {
        Field campo = objetivo.getClass().getDeclaredField(nombreCampo);
        campo.setAccessible(true);
        campo.set(objetivo, valor);
    }
}
