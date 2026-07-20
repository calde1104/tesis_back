package back.tesis.modules.sales;

import back.tesis.dto.sales.MonthlyRevenueDto;
import back.tesis.dto.sales.SaleItemDto;
import back.tesis.dto.sales.CategoryFilterOptionDto;
import back.tesis.dto.sales.SalesFilterOptionsDto;
import back.tesis.dto.sales.SalesSummaryDto;
import back.tesis.mapper.sales.SalesMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SalesService {

    private final SalesRepository salesRepository;
    private final MarcaFilterRepository marcaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final CategoriaRepository categoriaRepository;

    public SalesService(
            SalesRepository salesRepository,
            MarcaFilterRepository marcaRepository,
            DepartamentoRepository departamentoRepository,
            CategoriaRepository categoriaRepository
    ) {
        this.salesRepository = salesRepository;
        this.marcaRepository = marcaRepository;
        this.departamentoRepository = departamentoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public Page<SaleItemDto> listSales(
            String empresaId, List<Integer> anios, List<Integer> meses,
            String marcaId, Integer categoriaId, Integer departamentoId,
            Pageable pageable
    ) {
        return salesRepository.findSales(empresaId, anios, meses, marcaId, categoriaId, departamentoId, pageable)
                .map(SalesMapper::toDto);
    }

    public SalesSummaryDto getSummary(String empresaId, Integer anio, Integer mes) {
        SalesRepository.SummaryRow row = salesRepository.findSummary(empresaId, anio, mes);
        long totalUnidades = row.getTotalUnidades() != null ? row.getTotalUnidades() : 0L;
        BigDecimal totalFacturado = row.getTotalFacturado() != null ? row.getTotalFacturado() : BigDecimal.ZERO;
        return new SalesSummaryDto(totalUnidades, totalFacturado);
    }

    public List<MonthlyRevenueDto> getMonthlyRevenue(String empresaId) {
        return salesRepository.findMonthlyRevenue(empresaId).stream()
                .map(row -> new MonthlyRevenueDto(row.getAnio(), row.getMes(), row.getMontoFacturado()))
                .toList();
    }

    public SalesFilterOptionsDto getFilterOptions(String empresaId) {
        var marcas = marcaRepository.findByEmpresaIdOrderByNombre(empresaId).stream()
                .map(SalesMapper::toDto)
                .toList();
        var departamentos = departamentoRepository.findAllByOrderByNombre().stream()
                .map(SalesMapper::toDto)
                .toList();
        var categorias = categoriaRepository.findAllByOrderByNombre().stream()
                .map(SalesMapper::toCategoryFilterOptionDto)
                .toList();
        return new SalesFilterOptionsDto(marcas, departamentos, categorias);
    }
}
