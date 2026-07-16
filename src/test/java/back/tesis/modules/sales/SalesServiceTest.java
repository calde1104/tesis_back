package back.tesis.modules.sales;

import back.tesis.entities.Departamento;
import back.tesis.entities.Marca;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesServiceTest {

    @Mock
    private SalesRepository salesRepository;
    @Mock
    private MarcaFilterRepository marcaRepository;
    @Mock
    private DepartamentoRepository departamentoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;

    @Test
    void getSummaryMapsRepositoryRowToDto() {
        SalesRepository.SummaryRow row = new SalesRepository.SummaryRow() {
            public Long getTotalUnidades() {
                return 1000L;
            }

            public BigDecimal getTotalFacturado() {
                return new BigDecimal("50000.00");
            }
        };
        when(salesRepository.findSummary("emp-1", 2024, null)).thenReturn(row);

        SalesService service = new SalesService(salesRepository, marcaRepository, departamentoRepository, categoriaRepository);

        var result = service.getSummary("emp-1", 2024, null);

        assertThat(result.totalUnidades()).isEqualTo(1000L);
        assertThat(result.totalFacturado()).isEqualByComparingTo("50000.00");
    }

    @Test
    void getFilterOptionsScopesMarcasByEmpresaAndKeepsCatalogsGlobal() throws Exception {
        Marca marca = newMarca("emp-1", "Marca X");
        Departamento depto = newDepartamento("Bebidas");
        when(marcaRepository.findByEmpresaIdOrderByNombre("emp-1")).thenReturn(List.of(marca));
        when(departamentoRepository.findAllByOrderByNombre()).thenReturn(List.of(depto));
        when(categoriaRepository.findAllByOrderByNombre()).thenReturn(List.of());

        SalesService service = new SalesService(salesRepository, marcaRepository, departamentoRepository, categoriaRepository);

        var result = service.getFilterOptions("emp-1");

        assertThat(result.marcas()).hasSize(1);
        assertThat(result.departamentos()).hasSize(1);
    }

    private Marca newMarca(String empresaId, String nombre) throws Exception {
        Marca marca = instantiate(Marca.class);
        set(marca, "empresaId", empresaId);
        set(marca, "nombre", nombre);
        return marca;
    }

    private Departamento newDepartamento(String nombre) throws Exception {
        Departamento departamento = instantiate(Departamento.class);
        set(departamento, "nombre", nombre);
        return departamento;
    }

    private <T> T instantiate(Class<T> type) throws Exception {
        var constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private void set(Object target, String field, Object value) throws Exception {
        var f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}
