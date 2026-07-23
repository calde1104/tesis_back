package back.tesis.modules.sales;

import back.tesis.dto.sales.SaleItemDto;
import back.tesis.entities.Departamento;
import back.tesis.entities.Marca;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    void listSalesFiltersyByMultipleAniosAndMeses() {
        Pageable pageable = PageRequest.of(0, 10);
        SalesRepository.SaleRow row = mockSaleRow();
        Page<SalesRepository.SaleRow> page = new PageImpl<>(List.of(row), pageable, 1);
        when(salesRepository.findSales(
                eq("emp-1"), eq(List.of(2023, 2024)), eq(List.of(3, 4)),
                isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(page);

        SalesService service = new SalesService(salesRepository, marcaRepository, departamentoRepository, categoriaRepository);

        Page<SaleItemDto> result = service.listSales(
                "emp-1", List.of(2023, 2024), List.of(3, 4), null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).sku()).isEqualTo("SKU-1");
        assertThat(result.getContent().get(0).marcaNombre()).isEqualTo("Marca X");
        assertThat(result.getContent().get(0).categoriaNombre()).isEqualTo("Bebidas");
    }

    private SalesRepository.SaleRow mockSaleRow() {
        return new SalesRepository.SaleRow() {
            public String getProductoId() { return "prod-1"; }
            public int getAnio() { return 2024; }
            public int getMes() { return 3; }
            public int getUnidadesVendidas() { return 10; }
            public BigDecimal getMontoFacturado() { return new BigDecimal("500.00"); }
            public BigDecimal getPrecioUnitario() { return new BigDecimal("50.00"); }
            public String getSku() { return "SKU-1"; }
            public String getDescripcionProducto() { return "Producto uno"; }
            public String getMarcaNombre() { return "Marca X"; }
            public String getCategoriaNombre() { return "Bebidas"; }
            public Float getContenidoNeto() { return 900f; }
            public String getUnidadMedida() { return "ml"; }
        };
    }

    @Test
    void getFilterOptionsScopesMarcasDepartamentosAndCategoriasByEmpresa() throws Exception {
        Marca marca = newMarca("emp-1", "Marca X");
        Departamento depto = newDepartamento("Bebidas");
        when(marcaRepository.findByEmpresaIdOrderByNombre("emp-1")).thenReturn(List.of(marca));
        when(departamentoRepository.findByEmpresaIdOrderByNombre("emp-1")).thenReturn(List.of(depto));
        when(categoriaRepository.findByEmpresaIdOrderByNombre("emp-1")).thenReturn(List.of());

        SalesService service = new SalesService(salesRepository, marcaRepository, departamentoRepository, categoriaRepository);

        var result = service.getFilterOptions("emp-1");

        assertThat(result.marcas()).hasSize(1);
        assertThat(result.departamentos()).hasSize(1);
        assertThat(result.categorias()).isEmpty();
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
